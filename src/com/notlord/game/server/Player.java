package com.notlord.game.server;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.ITick;
import com.notlord.game.abilities.Ability;
import com.notlord.game.abilities.AbilityTree;
import com.notlord.game.abilities.AbilityTreeNode;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.AnimatedSprite;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.IClientInstance;
import com.notlord.networking.packets.*;
import com.notlord.utils.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.notlord.utils.Utils.sleep;

public class Player extends Entity implements ITick {
	private IClientInstance clientInstance;
	public final AbilityTree abilityTree;
	public final List<String> entitiesInFOV = new ArrayList<>();
	public final List<Ability> abilities = new ArrayList<>();
	protected final Vector2f size;
	public Player(float x, float y, String sprite, String abilityTreeType) {
		super(x, y,3,sprite);
		size = new Vector2f(24,32);
		abilityTree = AbilityTree.AbilityTreeType.valueOf(abilityTreeType.toUpperCase()).getNewAbilityTree(this);
		getStatHandler().setStat(Stat.MAX_HEALTH, 100);
		getStatHandler().setStat(Stat.HEALTH, 100);
		getStatHandler().setStat(Stat.DASH_SPEED, 120);
		getStatHandler().setStat(Stat.MAX_EXP, Constants.PLAYER_BASE_EXP_REQ);
		getStatHandler().setStat(Stat.LEVEL, 1);
		getStatHandler().setStat(Stat.MANA, 100);
		getStatHandler().setStat(Stat.MAX_MANA, 100);
		getStatHandler().setStat(Stat.MANA_REGEN, 5);
		getStatHandler().addStat(Stat.AP,500);
	}

	public void setClientInstance(IClientInstance clientInstance) {
		this.clientInstance = clientInstance;
		for(Stat stat : Stat.values())
			clientInstance.send(new StatUpdatePacket(stat, getStat(stat)));
	}

	public IClientInstance getClientInstance() {
		return clientInstance;
	}

	public void addAbility(Ability ability){
		if(abilities.contains(ability)) return;
		abilities.add(ability);
		ability.addFlags(this);
		if(clientInstance != null){
			clientInstance.send(new SetAbilityPacket(ability, abilities.size()-1));
		}
	}

	public boolean dashing = false;
	protected boolean attacking = false;
	public void handleMouseInput(int x, int y, int button){
		if(dashing || attacking || !isAlive()) return;
		switch (button){
			case 1 -> {
				// left click
				clientInstance.send(new PlayerStunPacket(true));
				attacking = true;
				Main.serverHandler.server.sendAll(new AnimationPacket(id,"melee_attack",2));
				spriteHandler.setCurrentAnimationWFlip("melee_attack");
				if(getX() < x && isFlipped()){
					flip();
				}
				else if(getX() > x && !isFlipped()){
					flip();
				}
				//
				AtomicInteger id = new AtomicInteger();
				id.set(Main.scheduler.scheduleSequence(
						spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.START),
						(Runnable) () -> {
							if(!dashing) {
								boolean no = false;
								for (AbilityTreeNode node : abilityTree.getNodes()) {
									if (node.isOwned()) {
										no = no || node.event.performMeleeAttack(this,x,y);
									}
								}
								if(no) {
									return;
								}
								new PlayerDamageBox(DamageSource.PLAYER, Stat.calculateAttack(getStat(Stat.ATTACK)),
										getBoundingBox().moveX(spriteHandler.isCurrentAnimationFlipped() ? -size.x * spriteMult / 2 : size.x * spriteMult / 2),
										spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END), this).init();
							}
							else{
								attacking = false;
								clientInstance.send(new PlayerStunPacket(false));
								Main.scheduler.removeSequence(id.get());
							}
						},
						spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END),
						(Runnable) () -> {
							attacking = false;
							clientInstance.send(new PlayerStunPacket(false));
						}
				));
			}
		}
	}

	public boolean isFlipped(){
		return spriteHandler.isCurrentAnimationFlipped();
	}

	public void flip(){
		boolean b = spriteHandler.isCurrentAnimationFlipped();
		spriteHandler.setCurrentAnimationFlipped(!b);
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"NULL",isFlipped() ? 1 : 0));
	}

	@Override
	public void causeDamage(Entity damager, float damage) {
		if(dashing || !isAlive()) return;
		//
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
		spriteHandler.setCurrentAnimationWFlip("hit");
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"hit",2));
		attacking = true;
		clientInstance.send(new PlayerStunPacket(true));
		Main.scheduler.scheduleDelay(() -> {
			attacking = false;
			clientInstance.send(new PlayerStunPacket(false));
		}, spriteHandler.getAnimationLengthInSeconds());
	}

	@Override
	public void handleDeath() {
		for (AbilityTreeNode node : abilityTree.getNodes()) {
			if (node.isOwned()) {
				if(node.event.deathEvent(this)) return;
			}
		}
		setAlive(false);
		spriteHandler.setCurrentAnimationWFlip("death");
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"death",2));
		clientInstance.send(new PlayerStunPacket(true));
		Main.scheduler.scheduleDelay(() -> Main.serverHandler.removeEntity(this),spriteHandler.getAnimationLengthInSeconds());
	}

	private final List<String> heldDownKeys = new ArrayList<>();
	public void handleInput(InputPacket packet){
		if(packet.getState() == 0 && !heldDownKeys.contains(packet.getKey())){
			heldDownKeys.add(packet.getKey());
		}
		else if(packet.getState() == 1){
			heldDownKeys.remove(packet.getKey());
		}
	}

	public List<String> getHeldDownKeys() {
		return heldDownKeys;
	}

	@Override
	public void nextTick(float deltaTime) {
		super.nextTick(deltaTime);
		if(!isAlive()) return;
		//
		if(getStat(Stat.HEALTH) < getStat(Stat.MAX_HEALTH) && getStat(Stat.HEALTH_REGEN) != 0){
			addStat(Stat.HEALTH, deltaTime * getStat(Stat.HEALTH_REGEN));
			if(getStat(Stat.HEALTH) > getStat(Stat.MAX_HEALTH)){
				setStat(Stat.HEALTH, getStat(Stat.MAX_HEALTH));
			}
		}
		//
		if(getStat(Stat.MANA) < getStat(Stat.MAX_MANA)){
			addStat(Stat.MANA, deltaTime * getStat(Stat.MANA_REGEN));
			if(getStat(Stat.MANA) > getStat(Stat.MAX_MANA)){
				setStat(Stat.MANA, getStat(Stat.MAX_MANA));
			}
		}
		if(getClientInstance() == null) return;
		// handle input
		if(heldDownKeys.contains("dash") && !dashing && !spriteHandler.getCurrentAnimation().equals("special_attack")){
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
			new Thread(() -> {
				spriteHandler.setCurrentAnimationWFlip("slide");
				spriteHandler.setCurrentAnimationFlipped(dir.x < 0);
				sleep(spriteHandler.getAnimationStateLengthInMillis(AnimatedSprite.AnimState.START));
				//
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
				sleep(spriteHandler.getAnimationStateLengthInMillis(AnimatedSprite.AnimState.END));
				//
				dashing = false;
			}, id+"_dash").start();
			return;
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
		String animation = "NULL";
		int flip = 2;
		if(heldDownKeys.contains("move_up") || heldDownKeys.contains("move_down")) {
			if (!spriteHandler.getCurrentAnimation().equals("running") && heldDownKeys.contains("sprint")) {
				animation = "running";
			}
			if (!spriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeys.contains("sprint")) {
				animation = "walking";
			}
		}
		if(heldDownKeys.contains("move_right") && !heldDownKeys.contains("move_left")) {
			if(spriteHandler.isCurrentAnimationFlipped()){
				flip = 0;
			}

			if(!spriteHandler.getCurrentAnimation().equals("running") && heldDownKeys.contains("sprint")){
				animation = "running";
			}
			else if(!spriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeys.contains("sprint")){
				animation = "walking";
			}
		}
		if(heldDownKeys.contains("move_left") && !heldDownKeys.contains("move_right")) {
			if(!spriteHandler.isCurrentAnimationFlipped()){
				flip = 1;
			}

			if (!spriteHandler.getCurrentAnimation().equals("running") && heldDownKeys.contains("sprint")) {
				animation = "running";
			}
			else if (!spriteHandler.getCurrentAnimation().equals("walking") && !heldDownKeys.contains("sprint")) {
				animation = "walking";
			}
		}
		if((!heldDownKeys.contains("move_up") && !heldDownKeys.contains("move_left")
				&& !heldDownKeys.contains("move_down") && !heldDownKeys.contains("move_right"))
				&& (spriteHandler.getCurrentAnimation().equals("walking") || spriteHandler.getCurrentAnimation().equals("running"))) {
			animation = "";
			flip = 2;
		}
		if(flip != 2){
			spriteHandler.setCurrentAnimationFlipped(flip == 1);
			Main.serverHandler.server.sendAll(new AnimationPacket(id, "NULL", flip));
		}
		if(!animation.equals("NULL")){
			spriteHandler.setCurrentAnimationWFlip(animation);
			Main.serverHandler.server.sendAll(new AnimationPacket(id, animation, 2));
		}
	}


	public float getStat(Stat stat){
		return getStatHandler().getStat(stat);
	}

	public float addStat(Stat stat, float n){
		clientInstance.send(new StatUpdatePacket(stat,getStatHandler().addStat(stat,n)));
		return getStat(stat);
	}

	public void setStat(Stat stat, float n){
		getStatHandler().setStat(stat,n);
		clientInstance.send(new StatUpdatePacket(stat,getStat(stat)));
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		super.render(g, offsetX, offsetY);
	}

	@Override
	public AABB getBoundingBox() {
		AABB aabb = new AABB(position.x,position.y,position.x+(size.x*spriteMult), position.y+(size.y*spriteMult));
		aabb.moveX(-size.x*spriteMult/2);
		aabb.moveY(-size.x*spriteMult);
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
		aabb.min.y += h*2/3;
		//aabb.max.y += h/3;
		float w = aabb.getWidth();
		aabb.min.x += w/6;
		aabb.max.x -= w/6;
		return aabb;
	}
}
