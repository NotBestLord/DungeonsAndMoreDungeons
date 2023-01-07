package com.notlord.game.client;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.Controls;
import com.notlord.game.Entity;
import com.notlord.game.PlayerHUD;
import com.notlord.game.abilities.Ability;
import com.notlord.game.abilities.AbilityTree;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.AnimatedSprite;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.AnimationPacket;

import java.util.ArrayList;
import java.util.List;

public class ClientPlayer extends Entity {
	private final Vector2f size = new Vector2f(24,32);
	public final PlayerHUD hud = new PlayerHUD(this);
	public final List<Ability> abilities = new ArrayList<>();
	public final AbilityTree abilityTree = AbilityTree.AbilityTreeType.valueOf(Constants.joinAbilityTreeType.toUpperCase()).getNewAbilityTree(this);
	public ClientPlayer(String id, Vector2f position, float spriteMult, String sprite) {
		super(position, spriteMult, sprite, id);
		getStatHandler().setStat(Stat.DASH_SPEED, 120);
		Main.getPanel().addObject(hud);
		setFlag("ability_tree_open_held", 0);
		setFlag("ability_tree_open", 0);
	}

	public int stunned = 0;

	private final List<Integer> heldDownKeycodes = Main.clientHandler.getPanelLogic().heldDownChars;
	@Override
	public void nextAnimationTick(float deltaTime) {
		super.nextAnimationTick(deltaTime);
		//
		if(heldDownKeycodes.contains(Controls.getKeyCode("ability_tree"))){
			if(getFlagI("ability_tree_open_held") == 0){
				setFlag("ability_tree_open_held", 1);
				if(getFlagI("ability_tree_open") == 0){
					Main.getPanel().addObject(abilityTree);
					setFlag("ability_tree_open", 1);
				}
				else{
					Main.getPanel().removeObject(abilityTree);
					setFlag("ability_tree_open", 0);
				}
			}
		}
		else if(getFlagI("ability_tree_open_held") == 1){
			setFlag("ability_tree_open_held",0);
		}
		//
		for(String key : List.of("ability_1","ability_2","ability_3","ability_4","ability_5")){
			if(heldDownKeycodes.contains(Controls.getKeyCode(key))){
				try{
					if(getFlagI(key) == 0) {
						int i = Integer.parseInt(key.split("_")[1]) - 1;
						if (abilities.size() > i) {
							abilities.get(i).activateClient(this);
						}
						setFlag(key, 1);
					}
				}
				catch (Exception ignored) {}
			}
			else if(getFlagI(key) != 0){
				setFlag(key, 0);
			}
		}
		//
		if(heldDownKeycodes.contains(Controls.getKeyCode("dash")) && !spriteHandler.getCurrentAnimation().equals("special_attack")
				&& !spriteHandler.getCurrentAnimation().equals("slide")){
			Vector2f dir = new Vector2f();
			for(int key : new ArrayList<>(heldDownKeycodes)) {
				if (key == Controls.getKeyCode("move_up")) {
					dir.y = -1;
				} else if (key == Controls.getKeyCode("move_down")) {
					dir.y = 1;
				} else if (key == Controls.getKeyCode("move_left")) {
					dir.x = -1;
				} else if (key == Controls.getKeyCode("move_right")) {
					dir.x = 1;
				}
			}
			dir.mul(getStatHandler().getStat(Stat.DASH_SPEED)*2);
			spriteHandler.setCurrentAnimationWFlip("slide");
			spriteHandler.setCurrentAnimationFlipped(dir.x < 0);
			Main.serverHandler.server.sendAll(new AnimationPacket(id, "slide", dir.x < 0 ? 1 : 0));
			stunned++;
			new Thread(() -> {
				try{
					Thread.sleep(spriteHandler.getAnimationStateLengthInMillis(AnimatedSprite.AnimState.START));
				}
				catch (Exception ignored) {}
				AABB pbb = getPhysicsBoundingBox();
				float t = spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.MIDDLE);
				float dif = 0;
				long start = System.nanoTime();
				while(dif < t){
					long last = System.nanoTime();
					long sub = last - start;
					start = last;
					float dt = (float) (sub / Constants.NANOSECOND);
					dif += dt;
					if(!Main.clientHandler.getCurrentRoom().isOutOfBounds(pbb.moveX(dir.x*dt).moveY(dir.y*dt))){
						getPosition().add(new Vector2f(dir.x*dt, dir.y*dt));
						pbb = getPhysicsBoundingBox();
					}
					else{
						break;
					}
					try{
						Thread.sleep(0,10000);
					} catch (InterruptedException ignored) {}
				}
				while(dif <= t){
					long last = System.nanoTime();
					long sub = last - start;
					start = last;
					float dt = (float) (sub / Constants.NANOSECOND);
					dif += dt;
				}
				try{
					Thread.sleep(spriteHandler.getAnimationStateLengthInMillis(AnimatedSprite.AnimState.END));
				}
				catch (Exception ignored) {}
				stunned--;
			}, id+"_dash").start();
			return;
		}
		if(stunned != 0) return;
		float moveSpeed = Stat.calculateSpeed(getStatHandler().getStat(Stat.SPEED)) * deltaTime * (heldDownKeycodes.contains(Controls.getKeyCode("sprint")) ? 1.75f : 1f);
		Vector2f position = new Vector2f(getPosition());
		AABB pbb = getPhysicsBoundingBox();
		for(int key : heldDownKeycodes){
			if (key == Controls.getKeyCode("move_up")) {
				position.y -= moveSpeed;
				pbb.moveY(-moveSpeed);
			} else if (key == Controls.getKeyCode("move_down")) {
				position.y += moveSpeed;
				pbb.moveY(moveSpeed);
			} else if (key == Controls.getKeyCode("move_left")) {
				position.x -= moveSpeed;
				pbb.moveX(-moveSpeed);
			} else if (key == Controls.getKeyCode("move_right")) {
				position.x += moveSpeed;
				pbb.moveX(moveSpeed);
			}
			if (Main.clientHandler.getCurrentRoom() != null && !Main.clientHandler.getCurrentRoom().isOutOfBounds(pbb)) {
				getPosition().set(position);
			}
			else{
				position = this.getPosition().clone();
			}
			pbb = getPhysicsBoundingBox();
		}
		//
		String animation = "NULL";
		int flip = 2;
		if(heldDownKeycodes.contains(Controls.getKeyCode("move_up")) || heldDownKeycodes.contains(Controls.getKeyCode("move_down"))){
			if (!spriteHandler.getCurrentAnimation().equals("running") && heldDownKeycodes.contains(Controls.getKeyCode("sprint"))) {
				animation = "running";
			}
			if (!spriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeycodes.contains(Controls.getKeyCode("sprint"))) {
				animation = "walking";
			}
		}
		if(heldDownKeycodes.contains(Controls.getKeyCode("move_right")) && !heldDownKeycodes.contains(Controls.getKeyCode("move_left"))){
			if(spriteHandler.isCurrentAnimationFlipped()){
				flip = 0;
			}

			if(!spriteHandler.getCurrentAnimation().equals("running") && heldDownKeycodes.contains(Controls.getKeyCode("sprint"))){
				animation = "running";
			}
			else if(!spriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeycodes.contains(Controls.getKeyCode("sprint"))){
				animation = "walking";
			}
		}
		if(heldDownKeycodes.contains(Controls.getKeyCode("move_left")) && !heldDownKeycodes.contains(Controls.getKeyCode("move_right"))) {
			if(!spriteHandler.isCurrentAnimationFlipped()){
				flip = 1;
			}

			if (!spriteHandler.getCurrentAnimation().equals("running") && heldDownKeycodes.contains(Controls.getKeyCode("sprint"))) {
				animation = "running";
			}
			else if (!spriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeycodes.contains(Controls.getKeyCode("sprint"))) {
				animation = "walking";
			}
		}
		if((!heldDownKeycodes.contains(Controls.getKeyCode("move_up")) && !heldDownKeycodes.contains(Controls.getKeyCode("move_left"))
				&& !heldDownKeycodes.contains(Controls.getKeyCode("move_down")) && !heldDownKeycodes.contains(Controls.getKeyCode("move_right")))
				&& (spriteHandler.getCurrentAnimation().equals("walking") || spriteHandler.getCurrentAnimation().equals("running"))){
			animation = "";
			flip = 2;
		}
		if(!animation.equals("NULL")){
			spriteHandler.setCurrentAnimationWFlip(animation);
		}
		if(flip != 2){
			spriteHandler.setCurrentAnimationFlipped(flip == 1);
		}
	}

	@Override
	public AABB getBoundingBox() {
		AABB aabb = new AABB(position.x,position.y,position.x+(size.x*spriteMult), position.y+(size.y*spriteMult));
		aabb.moveX(-size.x*spriteMult/2);
		aabb.moveY(-size.y*spriteMult);
		float h = aabb.getHeight();
		aabb.min.y += h/8;
		aabb.max.y -= h/8;
		float w = aabb.getWidth();
		aabb.min.x += w/6;
		aabb.max.x -= w/6;
		return aabb;
	}

	public AABB getPhysicsBoundingBox(){
		AABB aabb = getBoundingBox();
		float h = aabb.getHeight();
		aabb.min.y += h;
		aabb.max.y += h/3;
		return aabb;
	}
}
