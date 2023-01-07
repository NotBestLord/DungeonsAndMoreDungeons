package com.notlord;

import com.notlord.game.Entity;
import com.notlord.game.Particle;
import com.notlord.game.ParticleAnim;
import com.notlord.game.abilities.AbilityTree;
import com.notlord.game.client.ClientPlayer;
import com.notlord.game.client.ProjectileAnimEntity;
import com.notlord.game.client.ProjectileEntity;
import com.notlord.game.map.Room;
import com.notlord.game.map.RoomDecor;
import com.notlord.gui.panel.Panel;
import com.notlord.gui.panel.PanelClientLogic;
import com.notlord.gui.panel.PanelPreset;
import com.notlord.math.Vector2f;
import com.notlord.networking.Client;
import com.notlord.networking.listeners.ClientListener;
import com.notlord.networking.packets.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements ClientListener {
	public final Client client = new Client("",0);
	private final Panel panel;
	private final PanelClientLogic panelLogic;
	public PanelClientLogic getPanelLogic() {
		return panelLogic;
	}
	private final Map<String, Entity> entities = new HashMap<>();
	private ClientPlayer player;
	private Room currentRoom = null;
	public ClientPlayer getPlayer() {
		return player;
	}

	public ClientHandler(Panel panel) {
		client.addListener(this);
		panelLogic = new PanelClientLogic(this);
		this.panel = panel;
	}

	public void beginConnection(String host, int port){
		client.setAddress(host, port);
		try {
			client.start();
		}
		catch (Exception ignored){}
	}

	@Override
	public void connect() {
		panelLogic.enable();
		panel.clearObjects();
		sendPacket(new JoinPacket(Constants.joinAbilityTreeType));
	}

	@Override
	public void disconnect() {
		panelLogic.disable();
		entities.clear();
		panel.clearObjects();
		panel.addObject(PanelPreset.MAIN_MENU_JOIN.objects());
	}

	@Override
	public void receive(Object o) {
		if(o instanceof ARPlayerPacket packet){
			player = new ClientPlayer(packet.getSave().getId(), packet.getSave().getPosition(), packet.getSave().getSpriteMult(),
					AbilityTree.AbilityTreeType.valueOf(Constants.joinAbilityTreeType.toUpperCase()).getAssetKey());
			panelLogic.registerPlayer(player);
			entities.put(player.id, player);
			panel.addObject(player);
		}
		else if(o instanceof AREntityPacket packet){
			if(packet.getState() == 0){
				if(!entities.containsKey(packet.getSave().getId())) {
					entities.put(packet.getSave().getId(), packet.getSave().load());
					panel.addObject(entities.get(packet.getSave().getId()));
				}
			}
			else{
				entities.remove(packet.getSave().getId());
				panel.removeObjectWithId(packet.getSave().getId());
			}
		}
		else if(o instanceof RoomGeneratePacket packet){
			if(currentRoom != null){
				panel.removeObject(currentRoom);
				for (RoomDecor decor : currentRoom.decorations) {
					panel.removeObject(decor);
				}
			}
			currentRoom = new Room(packet.getKey());
			panel.addObject(currentRoom);
			for (RoomDecor decor : currentRoom.decorations) {
				panel.addObject(decor);
			}
			currentRoom.enemies = packet.getN();
		}
		else if(o instanceof ProjectilePacket packet){
			ProjectileEntity entity = new ProjectileEntity(packet.getPosition().x,packet.getPosition().y,
					packet.getSize(),packet.getAngle(),packet.getDuration(),packet.getSpriteKey(),packet.getVelocity());
			entities.put(packet.getId(), entity);
			panel.addObject(entity);
		}
		else if(o instanceof ProjectileAnimPacket packet){
			ProjectileAnimEntity entity = new ProjectileAnimEntity(packet.getSpriteKey(),packet.getPosition().x,packet.getPosition().y
					,packet.getAngle(),packet.getDuration(),packet.getVelocity());
			entities.put(packet.getId(), entity);
			panel.addObject(entity);
		}
		else if(o instanceof EntityPositionPacket packet){
			if(player != null && packet.getId().equals(player.id)){
				float dist = packet.getPosition().distance(new Vector2f(player.getX(),player.getY()));
				if(dist > Constants.CLIENT_PLAYER_POSITION_ERROR){
					player.setX(packet.getPosition().x);
					player.setY(packet.getPosition().y);
				}
			}
			else if(entities.containsKey(packet.getId())) {
				entities.get(packet.getId()).setX(packet.getPosition().x);
				entities.get(packet.getId()).setY(packet.getPosition().y);
			}
		}
		else if(o instanceof AnimationPacket packet){
			Entity e;
			if((e = entities.get(packet.getId())) != null){
				if(!packet.getAnimation().equals("NULL")) {
					e.spriteHandler.setCurrentAnimationWFlip(packet.getAnimation());
				}
				switch (packet.getState()){
					case 0 -> e.spriteHandler.setCurrentAnimationFlipped(false);
					case 1 -> e.spriteHandler.setCurrentAnimationFlipped(true);
				}
				e.spriteHandler.setRotation(packet.getRotation());
			}
		}
		else if(o instanceof EntityVisibilityPacket packet){
			Entity entity;
			if((entity = entities.get(packet.getId())) != null){
				entity.setVisibility(packet.isVisible());
			}
		}
		else if(o instanceof ExitPercentPacket packet){
			currentRoom.setExitProgress(packet.getPercent());
			if(currentRoom.enemies != 0) currentRoom.enemies = 0;
		}
		else if(o instanceof SpawnAnimatedParticlePacket packet){
			panel.addObject(new ParticleAnim(packet.getSpriteKey(), packet.getAnimationState(), packet.getPosition(),
					packet.getSize(), packet.getLifeTime(), packet.getAngle()));
		}
		else if(o instanceof SpawnParticlePacket packet){
			panel.addObject(new Particle(packet.getSpriteKey(), packet.getPosition(),
					packet.getSize(), packet.getLifeTime(), packet.getVelocityMul(), packet.getAngle()));
		}
		else if(o instanceof StatUpdatePacket packet){
			player.getStatHandler().setStat(packet.getStat(), packet.getN());
		}
		else if(o instanceof PlayerStunPacket packet){
			player.stunned += packet.isStunned() ? 1 : -1;
		}
		else if(o instanceof PopTextPacket packet){
			panel.addObject(packet.getPopText());
		}
		else if(o instanceof AddToAbilityTreePacket packet){
			player.abilityTree.getNode(packet.getKey()).setOwned();
		}
		else if(o instanceof SetAbilityPacket packet){
			if(player.abilities.size() == packet.getI()) player.abilities.add(packet.getAbility());
			else player.abilities.set(packet.getI(), packet.getAbility());
		}
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	public void sendPacket(Object o){
		client.send(o);
	}

	public void removeEntity(Entity entity){
		panel.removeObject(entity);
		for (String s : new ArrayList<>(entities.keySet())) {
			if(entity.getId().equals(s)){
				entities.remove(s);
			}
		}
	}

	public Room getCurrentRoom() {
		return currentRoom;
	}
}
