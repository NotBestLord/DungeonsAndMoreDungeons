package com.notlord.game.server;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.PlayerHUD;
import com.notlord.game.abilities.AbilityTree;
import com.notlord.game.abilities.AbilityTreeNode;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.AnimatedSprite;
import com.notlord.gui.images.AnimatedSpriteHandler;
import com.notlord.gui.images.SpriteUtils;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.AnimationPacket;
import com.notlord.utils.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClientPlayer but for the server-side
 */
public class ServerPlayer extends Player {
	public final AnimatedSpriteHandler mainSpriteHandler;
	public final PlayerHUD hud = new PlayerHUD(this);
	public ServerPlayer(float x, float y) {
		super(x, y, AbilityTree.AbilityTreeType.valueOf(Constants.hostAbilityTreeType.toUpperCase()).getAssetKey().replace("blue","red"),Constants.hostAbilityTreeType);
		mainSpriteHandler = SpriteUtils.getAnimatedSpriteHandler(AbilityTree.AbilityTreeType.valueOf(Constants.hostAbilityTreeType.toUpperCase()).getAssetKey());
		Main.getPanel().addObject(hud);
		setFlag("ability_tree_open_held", 0);
		setFlag("ability_tree_open", 0);
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		if(visibility) {
			mainSpriteHandler.draw(g,
					(int) (position.getIntX() - (mainSpriteHandler.getFrameWidth() * spriteMult / 2)) + offsetX,
					(int) (position.getIntY() - (mainSpriteHandler.getFrameHeight() * spriteMult)) + offsetY,
					(int) (position.getIntX() + (mainSpriteHandler.getFrameWidth() * spriteMult / 2)) + offsetX,
					position.getIntY() + offsetY);
			//getPhysicsBoundingBox().render(g,offsetX, offsetY);
		}
	}

	@Override
	public void nextAnimationTick(float dt) {
		mainSpriteHandler.nextTick(dt);
	}

	@Override
	public void nextTick(float deltaTime) {
		if(!isAlive()) return;
		super.nextTick(deltaTime);
		//
		List<String> heldDownKeys = getHeldDownKeys();
		// handle input
		if(heldDownKeys.contains("ability_tree")){
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
		for(String key : List.of("ability_1","ability_2","ability_3","ability_4","ability_5")){
			if(heldDownKeys.contains(key)){
				try{
					if(getFlagI(key) == 0) {
						int i = Integer.parseInt(key.split("_")[1]) - 1;
						if (abilities.size() > i) {
							abilities.get(i).activateServer(this);
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
		if(heldDownKeys.contains("dash") && !dashing && !mainSpriteHandler.getCurrentAnimation().equals("special_attack")){
			dashing = true;
			Vector2f dir = new Vector2f();
			for(String key : new ArrayList<>(heldDownKeys)) {
				switch (key) {
					case "move_up" -> dir.y = -1;
					case "move_down" -> dir.y = 1;
					case "move_left" -> dir.x = -1;
					case "move_right" -> dir.x = 1;
				}
			}
			dir.mul(getStat(Stat.DASH_SPEED)*2);
			mainSpriteHandler.setCurrentAnimationWFlip("slide");
			mainSpriteHandler.setCurrentAnimationFlipped(dir.x < 0);
			Main.serverHandler.server.sendAll(new AnimationPacket(id, "slide", dir.x < 0 ? 1 : 0));
			new Thread(() -> {
				try{
					Thread.sleep(mainSpriteHandler.getAnimationStateLengthInMillis(AnimatedSprite.AnimState.START));
				}
				catch (Exception ignored) {}
				AABB pbb = getPhysicsBoundingBox();
				float t = mainSpriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.MIDDLE);
				float dif = 0;
				long start = System.nanoTime();
				while(dif <= t){
					long last = System.nanoTime();
					long sub = last - start;
					start = last;
					float dt = (float) (sub / Constants.NANOSECOND);
					dif += dt;
					if(!Main.serverHandler.getCurrentRoom().isOutOfBounds(pbb.moveX(dir.x*dt).moveY(dir.y*dt))){
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
					Thread.sleep(mainSpriteHandler.getAnimationStateLengthInMillis(AnimatedSprite.AnimState.END));
				}
				catch (Exception ignored) {}
				dashing = false;
			}, id+"_dash").start();
		}
		if(attacking || dashing) return;
		float moveSpeed = Stat.calculateSpeed(getStat(Stat.SPEED)) * deltaTime * (heldDownKeys.contains("sprint") ? 1.75f : 1f);
		Vector2f position = new Vector2f(this.position);
		AABB pbb = getPhysicsBoundingBox();
		for(String key : new ArrayList<>(heldDownKeys)){
			switch (key){
				case "move_up" -> {
					position.y -= moveSpeed;
					pbb.moveY(-moveSpeed);
				}
				case "move_down" -> {
					position.y += moveSpeed;
					pbb.moveY(moveSpeed);
				}
				case "move_left" -> {
					position.x -= moveSpeed;
					pbb.moveX(-moveSpeed);
				}
				case "move_right" -> {
					position.x += moveSpeed;
					pbb.moveX(moveSpeed);
				}
			}
			//
			if (!Main.serverHandler.getCurrentRoom().isOutOfBounds(pbb)) {
				this.position.x = position.x;
				this.position.y = position.y;
			}
			else{
				position = new Vector2f(this.position);
			}
			pbb = getPhysicsBoundingBox();
		}
		//
		String animation = "NULL";
		int flip = 2;
		if(heldDownKeys.contains("move_up") || heldDownKeys.contains("move_down")) {
			if (!mainSpriteHandler.getCurrentAnimation().equals("running") && heldDownKeys.contains("sprint")) {
				animation = "running";
			}
			if (!mainSpriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeys.contains("sprint")) {
				animation = "walking";
			}
		}
		if(heldDownKeys.contains("move_right") && !heldDownKeys.contains("move_left")) {
			if(mainSpriteHandler.isCurrentAnimationFlipped()){
				flip = 0;
			}

			if(!mainSpriteHandler.getCurrentAnimation().equals("running") && heldDownKeys.contains("sprint")){
				animation = "running";
			}
			else if(!mainSpriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeys.contains("sprint")){
				animation = "walking";
			}
		}
		if(heldDownKeys.contains("move_left") && !heldDownKeys.contains("move_right")) {
			if(!mainSpriteHandler.isCurrentAnimationFlipped()){
				flip = 1;
			}

			if (!mainSpriteHandler.getCurrentAnimation().equals("running") && heldDownKeys.contains("sprint")) {
				animation = "running";
			}
			else if (!mainSpriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeys.contains("sprint")) {
				animation = "walking";
			}
		}
		if((!heldDownKeys.contains("move_up") && !heldDownKeys.contains("move_left")
				&& !heldDownKeys.contains("move_down") && !heldDownKeys.contains("move_right"))
				&& (mainSpriteHandler.getCurrentAnimation().equals("walking") || mainSpriteHandler.getCurrentAnimation().equals("running"))) {
			animation = "";
			flip = 2;
		}
		if(flip != 2){
			mainSpriteHandler.setCurrentAnimationFlipped(flip == 1);
			Main.serverHandler.server.sendAll(new AnimationPacket(id, "NULL", flip));
		}
		if(!animation.equals("NULL")){
			mainSpriteHandler.setCurrentAnimationWFlip(animation);
			Main.serverHandler.server.sendAll(new AnimationPacket(id, animation, 2));
		}
	}

	@Override
	public void handleMouseInput(int x, int y, int button){
		if(attacking || dashing || !isAlive()) return;
		if (button == 1) {
			// left click
			this.attacking = true;
			Main.serverHandler.server.sendAll(new AnimationPacket(id, "melee_attack", 2));
			mainSpriteHandler.setCurrentAnimationWFlip("melee_attack");
			if(getX() < x && isFlipped()){
				flip();
			}
			else if(getX() > x && !isFlipped()){
				flip();
			}
			//
			AtomicInteger id = new AtomicInteger();
			id.set(Main.scheduler.scheduleSequence(
					mainSpriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.START),
					(Runnable) () -> {
						if (!dashing) {
							boolean no = false;
							for (AbilityTreeNode node : abilityTree.getNodes()) {
								if (node.isOwned()) {
									no = no || node.event.performMeleeAttack(this,x,y);
								}
							}
							if(no) {
								Main.scheduler.scheduleDelay(() -> attacking = false,
										mainSpriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END) / 2f);
								Main.scheduler.removeSequence(id.get());
								return;
							}
							new PlayerDamageBox(DamageSource.PLAYER, Stat.calculateAttack(getStat(Stat.ATTACK)),
									getBoundingBox().moveX(mainSpriteHandler.isCurrentAnimationFlipped() ? -size.x * spriteMult / 2 : size.x * spriteMult / 2),
									mainSpriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END), this).init();
						} else {
							attacking = false;
							Main.scheduler.removeSequence(id.get());
						}
					},
					mainSpriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END),
					(Runnable) () -> attacking = false

			));
		}
	}

	@Override
	public boolean isFlipped(){
		return mainSpriteHandler.isCurrentAnimationFlipped();
	}

	@Override
	public void flip(){
		boolean b = mainSpriteHandler.isCurrentAnimationFlipped();
		mainSpriteHandler.setCurrentAnimationFlipped(!b);
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"NULL",isFlipped() ? 1 : 0));
	}

	@Override
	public void causeDamage(Entity damager, float damage) {
		if(dashing || !isAlive()) return;
		float f = 1f;
		for (AbilityTreeNode node : abilityTree.getNodes()) {
			if (node.isOwned()) {
				Tuple<Float, Boolean> t = node.event.takeDamage(this,damager);
				if(t.getK()) {
					f = 0;
					break;
				}
				f += t.getV();
			}
		}
		damage *= f;
		//
		addStat(Stat.HEALTH, -(damage * Stat.calculateDefense(getStat(Stat.DEFENSE)) / 100));
		if(getStat(Stat.HEALTH) <= 0){
			handleDeath();
		}
		//
		mainSpriteHandler.setCurrentAnimationWFlip("hit");
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"hit",2));
		attacking = true;
		Main.scheduler.scheduleDelay(() -> attacking = false, mainSpriteHandler.getAnimationLengthInSeconds());
	}

	@Override
	public void handleDeath() {
		for (AbilityTreeNode node : abilityTree.getNodes()) {
			if (node.isOwned()) {
				if(node.event.deathEvent(this)) return;
			}
		}
		setAlive(false);
		mainSpriteHandler.setCurrentAnimationWFlip("death");
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"death",2));
		Main.scheduler.scheduleDelay(() -> Main.serverHandler.removeEntity(this),mainSpriteHandler.getAnimationLengthInSeconds());
	}

	@Override
	public float getStat(Stat stat) {
		return getStatHandler().getStat(stat);
	}

	@Override
	public float addStat(Stat stat, float n) {
		return getStatHandler().addStat(stat, n);
	}

	@Override
	public void setStat(Stat stat, float n) {
		getStatHandler().setStat(stat, n);
	}
}
