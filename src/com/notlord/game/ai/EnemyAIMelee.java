package com.notlord.game.ai;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.server.DamageBox;
import com.notlord.game.server.DamageSource;
import com.notlord.game.server.Enemy;
import com.notlord.game.server.Player;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.AnimatedSprite;
import com.notlord.math.AABB;
import com.notlord.math.Intersections;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.AnimationPacket;
import com.notlord.utils.Tuple;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EnemyAIMelee extends EntityAI {
	private final float sightRadius;
	private final float sightLossRadius;

	public EnemyAIMelee(AABB field, float sightRadius, float sightLossRadius) {
		super(field);
		this.sightLossRadius = sightLossRadius;
		this.sightRadius = sightRadius;
		// select target
		actions.add((self, tick) -> {
			Player target = get("Target");
			for (Player p : Main.serverHandler.getAllPlayers()) {
				if(target == p && (p.getBoundingBox().getMiddle().distance(self.getBoundingBox().getMiddle()) >= sightLossRadius ||
						!Intersections.AABBIntersectsAABB(field,p.getBoundingBox()))){
					put("Target",null);
					target = null;
				}
				if((target == null || target.getStat(Stat.AGGRO) < p.getStat(Stat.AGGRO)) && p.getBoundingBox().getMiddle().distance(self.getBoundingBox().getMiddle()) <= sightRadius &&
						Intersections.AABBIntersectsAABB(field,p.getBoundingBox())){
					put("Target", p);
					target = p;
				}
			}
		});
		// move to target
		actions.add((self, tick) -> {
			if(get("Wait") != null) return;
			if(self.stunned != 0 || self.attacking) return;
			Player target = get("Target");
			boolean moved = false;
			if(target != null){
				if(!Intersections.AABBIntersectsAABB(field,target.getBoundingBox())) return;
				if(!self.spriteHandler.getCurrentAnimation().equals("running")){
					self.spriteHandler.setCurrentAnimationWFlip("running");
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "running",2));
				}
				Vector2f add = new Vector2f(self.getPosition().angle(target.getPosition())).mul(Stat.calculateSpeed(stats.get("SPEED"))).mul(tick);
				Vector2f pos = self.getBoundingBox().getMiddle().add(add);
				if(field.contains(pos)){
					moved = true;
					self.getPosition().add(add);
					if(self.spriteHandler.isCurrentAnimationFlipped() && target.getX() > self.getX()){
						self.spriteHandler.setCurrentAnimationFlipped(false);
						Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "NULL",1));
					}
					else if(!self.spriteHandler.isCurrentAnimationFlipped() && target.getX() < self.getX()){
						self.spriteHandler.setCurrentAnimationFlipped(true);
						Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "NULL",0));
					}
				}
			}
			if(moved){
				put("MovedToTarget",1);
			}
			else{
				remove("MovedToTarget");
			}
		});
		// attack
		actions.add((self, tick) -> {
			if(get("AttackCD") != null) return;
			if(self.stunned != 0 || self.attacking) return;
			Tuple<AABB, Float> damageBox = self.getTargetBox();
			AABB box1 = new AABB(damageBox.getV()).moveX(damageBox.getK());
			AABB box2 = new AABB(damageBox.getV()).moveX(-damageBox.getK());
			for (Player player : Main.serverHandler.getAllPlayers()) {
				if(Intersections.AABBIntersectsAABB(player.getBoundingBox(), box1)){
					self.spriteHandler.setCurrentAnimationFlipped(false);
					self.spriteHandler.setCurrentAnimationWFlip("melee_attack");
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "melee_attack", 2));
					self.attacking = true;
					put("AttackCD", 1);
					AtomicInteger id = new AtomicInteger();
					id.set(Main.scheduler.scheduleSequence(
							self.spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.START) + 0.25f,
							(Runnable) () -> {
								if(self.stunned == 0 && self.isAlive()) {
									Main.serverHandler.addDamageBox(new DamageBox(DamageSource.ENEMY, Stat.calculateAttack(self.getStatHandler().getStat(Stat.ATTACK)), box1, 0.2f, self));
								}
								else{
									self.attacking = false;
									remove("AttackCD");
									Main.scheduler.removeSequence(id.get());
								}
							},
							self.spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END) + 0.1f,
							(Runnable) ()-> self.attacking = false,
							1.5f,
							(Runnable) ()-> remove("AttackCD")
					));
				}
				else if(Intersections.AABBIntersectsAABB(player.getBoundingBox(), box2)) {
					put("AttackCD", 1);
					self.spriteHandler.setCurrentAnimationWFlip("melee_attack");
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "melee_attack",2));
					self.spriteHandler.setCurrentAnimationFlipped(true);
					self.attacking = true;
					AtomicInteger id = new AtomicInteger();
					id.set(Main.scheduler.scheduleSequence(
							self.spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.START) + 0.25f,
							(Runnable) () -> {
								if(self.stunned == 0 && self.isAlive()) {
									Main.serverHandler.addDamageBox(new DamageBox(DamageSource.ENEMY, Stat.calculateAttack(self.getStatHandler().getStat(Stat.ATTACK)), box2, 0.2f, self));
								}
								else{
									self.attacking = false;
									remove("AttackCD");
									Main.scheduler.removeSequence(id.get());
								}
							},
							self.spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END) + 0.1f,
							(Runnable) ()-> self.attacking = false,
							1.5f,
							(Runnable) ()-> remove("AttackCD")
					));
				}
			}
		});
		// patrol
		actions.add((self, tick) -> {
			if(get("Wait") != null || get("MovedToTarget") != null) return;
			if(self.stunned != 0 || self.attacking) return;
			Vector2f target = get("Patrol");
			if(target == null){
				put("Patrol", new Vector2f(field.getRandomPoint()));
				put("Wait", 1f);
			}
			else{
				if(!self.spriteHandler.getCurrentAnimation().equals("walking")){
					self.spriteHandler.setCurrentAnimation("walking");
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "walking",2));
				}
				if(!self.spriteHandler.isCurrentAnimationFlipped() && target.x < self.getX()){
					self.spriteHandler.setCurrentAnimationFlipped(true);
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "NULL",1));
				}
				else if(self.spriteHandler.isCurrentAnimationFlipped() && target.x > self.getX()){
					self.spriteHandler.setCurrentAnimationFlipped(false);
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "NULL",0));
				}
				self.getPosition().add(new Vector2f(self.getPosition().angle(target))
						.mul(Stat.calculateSpeed(stats.get("SPEED")) * 0.75f).mul(tick));
				if(self.getPosition().distance(target) <= Constants.AI_MIN_DIST){
					remove("Patrol");
				}
			}
		});
	}

	@Override
	public EntityAI duplicate(AABB field, Map<String, Float> stats){
		EnemyAIMelee aiMelee = new EnemyAIMelee(field, sightRadius, sightLossRadius);
		aiMelee.stats.putAll(stats);
		return aiMelee;
	}

	public void render(Enemy self, Graphics g, int offsetX, int offsetY) {
		g.setColor(Color.white);
		AABB area = new AABB(-sightRadius, -sightRadius, sightRadius, sightRadius);
		area.moveX(self.getBoundingBox().getMiddle().x);
		area.moveY(self.getBoundingBox().getMiddle().y);
		g.drawRect(area.min.getIntX() + offsetX, area.min.getIntY() + offsetY,
				area.getWidthInt(), area.getHeightInt());
		Tuple<AABB, Float> damageBox = self.getTargetBox();
		AABB box1 = new AABB(damageBox.getV()).moveX(damageBox.getK());
		AABB box2 = new AABB(damageBox.getV()).moveX(-damageBox.getK());
		g.setColor(Color.yellow);
		g.drawRect(box1.min.getIntX() + offsetX, box1.min.getIntY() + offsetY,
				box1.getWidthInt(), box1.getHeightInt());
		g.drawRect(box2.min.getIntX() + offsetX, box2.min.getIntY() + offsetY,
				box2.getWidthInt(), box2.getHeightInt());
	}

}
