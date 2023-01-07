package com.notlord;

import com.notlord.game.Entity;
import com.notlord.game.abilities.Ability;
import com.notlord.game.abilities.AbilityTree;
import com.notlord.game.map.Room;
import com.notlord.game.map.RoomDecor;
import com.notlord.game.server.DamageBox;
import com.notlord.game.server.Player;
import com.notlord.game.server.ServerEntityBuffer;
import com.notlord.game.server.ServerPlayer;
import com.notlord.gui.panel.Panel;
import com.notlord.gui.panel.PanelPreset;
import com.notlord.gui.panel.PanelServerLogic;
import com.notlord.math.Vector2f;
import com.notlord.networking.IClientInstance;
import com.notlord.networking.Server;
import com.notlord.networking.listeners.ServerListener;
import com.notlord.networking.packets.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerHandler implements ServerListener {
	public final Server server = new Server(0);
	private Vector2f startingLocation = new Vector2f(100,100);
	private final Map<Integer, Player> connectedPlayers = new HashMap<>();
	private final ServerEntityBuffer buffer = new ServerEntityBuffer(this, 0.02f);
	private final List<DamageBox> damageBoxes = new CopyOnWriteArrayList<>();
	private final Panel panel;
	private final PanelServerLogic panelLogic;
	private Room currentRoom;
	public ServerHandler(Panel panel) {
		server.addListener(this);
		panelLogic = new PanelServerLogic(this);
		this.panel = panel;
	}

	public List<Entity> getEntities(){
		return buffer.getAllEntities();
	}

	@Override
	public void clientConnect(IClientInstance client) {}

	@Override
	public void clientReceive(IClientInstance client, Object o) {
		if(o instanceof JoinPacket packet){
			Player player = new Player(startingLocation.x,startingLocation.y, AbilityTree.AbilityTreeType.valueOf(packet.getAbilityTreeType().toUpperCase()).getAssetKey().replace("blue","red"),
					packet.getAbilityTreeType());
			client.send(new ARPlayerPacket(player.save(), 0));
			player.setClientInstance(client);
			for(Entity entity : buffer.getAllEntities()){
				client.send(new AREntityPacket(entity.save(), 0));
			}
			connectedPlayers.put(client.getID(), player);
			panel.addObject(player);
			server.sendAllExclude(new AREntityPacket(player.save(), 0), client);
			buffer.addEntity(player);
			client.send(new RoomGeneratePacket(currentRoom.getKey(), currentRoom.enemies != 0 ? 1 : 0));
		}
		else if(o instanceof InputPacket packet && connectedPlayers.get(client.getID()) != null){
			Player player = connectedPlayers.get(client.getID());
			if(packet.getKey().equals("clear")){
				player.getHeldDownKeys().clear();
			}
			else{
				player.handleInput(packet);
			}
		}
		else if(o instanceof MouseInputPacket packet && connectedPlayers.get(client.getID()) != null){
			Player player = connectedPlayers.get(client.getID());
			player.handleMouseInput(packet.getX(), packet.getY(),packet.getButton());
		}
		else if(o instanceof AnimationPacket packet){
			Entity e = null;
			for (Entity entity : buffer.getAllEntities()) {
				if(entity.id.equals(packet.getId())){
					e = entity;
					break;
				}
			}
			if(e != null){
				if(!packet.getAnimation().equals("NULL") && !e.spriteHandler.getCurrentAnimation().equals(packet.getAnimation())) {
					e.spriteHandler.setCurrentAnimationWFlip(packet.getAnimation());
				}
				switch (packet.getState()){
					case 0 -> e.spriteHandler.setCurrentAnimationFlipped(false);
					case 1 -> e.spriteHandler.setCurrentAnimationFlipped(true);
				}
				e.spriteHandler.setRotation(packet.getRotation());
			}
			server.sendAllExclude(packet, client);
		}
		else if(o instanceof ActivateAbilityPacket packet && connectedPlayers.get(client.getID()) != null){
			Player player = connectedPlayers.get(client.getID());
			if(player.abilities.contains(packet.getAbility())){
				packet.getAbility().trigger(player, packet.getArgs());
			}
		}
		else if(o instanceof AbilityTreeBuyRequest packet && connectedPlayers.get(client.getID()) != null){
			Player player = connectedPlayers.get(client.getID());
			player.abilityTree.getNode(packet.getKey()).buyAttempt(packet.getX(),packet.getY());
		}
		else if(o instanceof AbilitySwitchPacket packet && connectedPlayers.get(client.getID()) != null){
			Player player = connectedPlayers.get(client.getID());
			int a = packet.getA();
			int b = packet.getB();
			if(a >= 0 && a < player.abilities.size() && b >= 0 && b < player.abilities.size()){
				Ability ability = player.abilities.get(a);
				player.abilities.set(a,player.abilities.get(b));
				player.abilities.set(b,ability);
				player.getClientInstance().send(new SetAbilityPacket(player.abilities.get(a), b));
				player.getClientInstance().send(new SetAbilityPacket(player.abilities.get(b), a));
			}
		}
	}

	@Override
	public void clientDisconnect(IClientInstance client) {
		Player player = connectedPlayers.get(client.getID());
		buffer.remove(player);
		server.sendAll(new AREntityPacket(player.save(), 1));
		panel.removeObject(player);
		connectedPlayers.remove(client.getID());
	}

	@Override
	public void serverClose() {
		panelLogic.disable();
		buffer.clear();
		panel.clearObjects();
		connectedPlayers.clear();
		panel.addObject(PanelPreset.MAIN_MENU.objects());
	}

	public void generateNewDungeon(){
		if(currentRoom != null) {
			List<Entity> entities = buffer.getAllEntities();
			for (Entity entity : entities) {
				if(!(entity instanceof Player))
					Main.serverHandler.removeEntity(entity);
			}
			panel.removeObject(currentRoom);
			for (RoomDecor decor : currentRoom.decorations) {
				panel.removeObject(decor);
			}
		}
		currentRoom = Room.selectRoom(getAllPlayers());
		panel.addObject(currentRoom);
		for (RoomDecor decor : currentRoom.decorations) {
			panel.addObject(decor);
		}
		startingLocation = currentRoom.getStartPos();
		for(Player player : new ArrayList<>(connectedPlayers.values())) {
			player.setX(startingLocation.x);
			player.setY(startingLocation.y);
			server.sendAll(new EntityPositionPacket(player.id, new Vector2f(player.getX(), player.getY())));
		}
		server.sendAll(new RoomGeneratePacket(currentRoom.getKey(), currentRoom.enemies != 0 ? 1 : 0));
	}

	public void start(){
		new Thread( () -> {
			currentRoom = Room.selectRoom(getAllPlayers());
			panel.addObject(currentRoom);
			for (RoomDecor decor : currentRoom.decorations) {
				panel.addObject(decor);
			}
			startingLocation = currentRoom.getStartPos();
			ServerPlayer self = new ServerPlayer(startingLocation.x, startingLocation.y);
			connectedPlayers.put(-1, self);
			addEntity(self);
			panelLogic.enable(self);
			//
			server.start();
			long last = System.nanoTime();
			double unprocessed = 0;
			double tps = 2000;
			while (!server.isRunning()) {
				Thread.onSpinWait();
			}
			while (server.isRunning()) {
				long start = System.nanoTime();
				long passed = start - last;
				last = start;
				unprocessed += passed / (double) 1000000000;
				if (unprocessed >= 1 / tps) {
					for (Entity entity : buffer.getAllEntities()) {
						try {
							entity.nextTick((float) unprocessed);
						}
						catch (Exception e){
							if(!(e.getMessage() != null && e.getMessage().contains("String.hashCode()"))){
								e.printStackTrace();
							}
						}
					}
					for (DamageBox box : damageBoxes) {
						box.nextTick((float) unprocessed);
					}
					if(currentRoom != null && !currentRoom.nextRoom){
						currentRoom.nextTick((float) unprocessed);
					}
					buffer.next((float) unprocessed);
				}
				while (unprocessed >= 1 / tps) {
					unprocessed -= 1 / tps;
				}
			}
		}).start();
	}

	public void addEntity(Entity e){
		panel.addObject(e);
		server.sendAll(new AREntityPacket(e.save(), 0));
		buffer.addEntity(e);
	}

	public void removeEntity(Entity e){
		panel.removeObject(e);
		server.sendAll(new AREntityPacket(e.save(), 1));
		buffer.remove(e);
	}

	public void addDamageBox(DamageBox box){
		damageBoxes.add(box);
		//panel.addObject(box);
	}

	public void removeDamageBox(DamageBox box){
		damageBoxes.remove(box);
		//panel.removeObject(box);
	}

	public ServerPlayer getPlayer(){
		return (ServerPlayer) connectedPlayers.get(-1);
	}

	public List<Player> getAllPlayers(){
		return new ArrayList<>(connectedPlayers.values());
	}

	public Room getCurrentRoom(){
		return currentRoom;
	}

	public PanelServerLogic getPanelLogic() {
		return panelLogic;
	}
}
