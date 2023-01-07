package com.notlord.game.ai;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.server.DamageSource;
import com.notlord.game.server.Player;
import com.notlord.game.server.Projectile;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.AnimatedSprite;
import com.notlord.math.AABB;
import com.notlord.math.Intersections;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.AnimationPacket;
import com.notlord.networking.packets.ProjectilePacket;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EnemyAIRanged extends EntityAI {
	private final float sightRadius;
	private final float sightLossRadius;
	private final float minRange, maxRange;
	private final long attackSpeed;
	private final String projectileSpriteKey;
	public EnemyAIRanged(float sightRadius, float sightLossRadius, float minRange, float maxRange, AABB field, float attackSpeed, String projectileSpriteKey) {
		this(sightRadius,sightLossRadius, minRange, maxRange, field,(long) attackSpeed*1000L,projectileSpriteKey);
	}

	private EnemyAIRanged(float sightRadius, float sightLossRadius, float minRange, float maxRange, AABB field, long attackSpeed, String projectileSpriteKey) {
		super(field);
		this.sightRadius = sightRadius;
		this.sightLossRadius = sightLossRadius;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.attackSpeed = attackSpeed;
		this.projectileSpriteKey = projectileSpriteKey;
		// select target
		actions.add((self, tick) -> {
			Player target = get("Target");
			for (Player p : Main.serverHandler.getAllPlayers()) {
				if(target == p && p.getBoundingBox().getMiddle().distance(self.getBoundingBox().getMiddle()) >= sightLossRadius){
					put("Target",null);
					target = null;
				}
				if((target == null || target.getStat(Stat.AGGRO) < p.getStat(Stat.AGGRO)) &&
						p.getBoundingBox().getMiddle().distance(self.getBoundingBox().getMiddle()) <= sightRadius &&
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
			boolean moved = target != null;
			if(target != null && target.getPosition().distance(self.getPosition()) < minRange){
				if(!Intersections.AABBIntersectsAABB(field,target.getBoundingBox())) return;
				if(!self.spriteHandler.getCurrentAnimation().equals("running")){
					self.spriteHandler.setCurrentAnimationWFlip("running");
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "running",2));
				}
				Vector2f add = new Vector2f(self.getPosition().angle(target.getPosition()) + 180).mul(Stat.calculateSpeed(stats.get("SPEED"))).mul(tick);
				Vector2f pos = self.getBoundingBox().getMiddle().add(add);
				if(field.contains(pos)){
					self.getPosition().add(add);
					if(self.spriteHandler.isCurrentAnimationFlipped() && target.getX() > self.getX()){
						self.spriteHandler.setCurrentAnimationFlipped(true);
						Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "NULL",1));
					}
					else if(!self.spriteHandler.isCurrentAnimationFlipped() && target.getX() < self.getX()){
						self.spriteHandler.setCurrentAnimationFlipped(false);
						Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "NULL",0));
					}
				}
			}
			else if(target != null && target.getPosition().distance(self.getPosition()) > maxRange){
				if(!Intersections.AABBIntersectsAABB(field,target.getBoundingBox())) return;
				if(!self.spriteHandler.getCurrentAnimation().equals("running")){
					self.spriteHandler.setCurrentAnimationWFlip("running");
					Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "running",2));
				}
				Vector2f add = new Vector2f(self.getPosition().angle(target.getPosition())).mul(Stat.calculateSpeed(stats.get("SPEED"))).mul(tick);
				Vector2f pos = self.getBoundingBox().getMiddle().add(add);
				if(field.contains(pos)){
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
				if (get("AttackCD") != null) return;
				if (self.stunned != 0 || self.attacking) return;
				Player target = get("Target");
				if (target == null) return;
				put("AttackCD", 0);
				self.attacking = true;
				float cd = stats.get("ATTACK_CD");
				Vector2f pos = self.getBoundingBox().getMiddle();
				self.spriteHandler.setCurrentAnimationWFlip("ranged_attack");
				Main.serverHandler.server.sendAll(new AnimationPacket(self.id, "ranged_attack", 2));
				AtomicInteger id = new AtomicInteger();
				id.set(Main.scheduler.scheduleSequence(
						self.spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.START) + 0.25f,
						(Runnable) () -> {
							if (self.stunned == 0 && self.isAlive()) {
								Vector2f targetPos = target.getBoundingBox().getMiddle();
								float finalAngle = self.getBoundingBox().getMiddle().angle(targetPos);
								Projectile projectile = new Projectile(projectileSpriteKey, DamageSource.ENEMY, self, new AABB(new Vector2f(pos).subtract(new Vector2f(stats.get("PROJ_SIZE"), stats.get("PROJ_SIZE"))),
										new Vector2f(pos).add(new Vector2f(stats.get("PROJ_SIZE"), stats.get("PROJ_SIZE")))), stats.get("PROJ_DURATION"), finalAngle, stats.get("PROJ_SPEED"), stats.get("PROJ_DAMAGE")
								);
								Main.serverHandler.server.sendAll(new ProjectilePacket(projectile.getId(),
										projectileSpriteKey, stats.get("PROJ_DURATION"), stats.get("PROJ_SIZE"),
										finalAngle, new Vector2f(pos), projectile.getVelocity()));
							} else {
								remove("AttackCD");
								self.attacking = false;
								Main.scheduler.removeSequence(id.get());
							}
						},
						self.spriteHandler.getAnimationStateLengthInSeconds(AnimatedSprite.AnimState.END) + 0.1f,
						(Runnable) () -> self.attacking = false,
						cd,
						(Runnable) () -> remove("AttackCD")
				));
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
		EnemyAIRanged aiRanged = new EnemyAIRanged(sightRadius, sightLossRadius, minRange, maxRange, field, attackSpeed, projectileSpriteKey);
		aiRanged.stats.putAll(stats);
		return aiRanged;
	}
}
