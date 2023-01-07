package com.notlord.game.server;

import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.ID;
import com.notlord.game.abilities.AbilityTreeNode;
import com.notlord.gui.images.AnimatedSpriteHandler;
import com.notlord.gui.images.SpriteUtils;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Intersections;
import com.notlord.math.Vector2f;
import com.notlord.utils.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectileAnim extends DamageBox implements IRender, ID {
	private final String id = UUID.randomUUID().toString();
	private final AnimatedSpriteHandler spriteHandler;
	protected final Vector2f velocity;
	protected List<Vector2f> lst = new ArrayList<>();
	protected final Entity shooter;
	public ProjectileAnim(String spriteKey, DamageSource source, Entity shooter, Vector2f startPos, float duration, float angle, float speed, float damage) {
		super(source, damage, generateBox(spriteKey, startPos, angle), duration, shooter);
		spriteHandler = SpriteUtils.getAnimatedSpriteHandler(spriteKey);
		spriteHandler.setRotation(angle);
		this.shooter = shooter;
		velocity = new Vector2f(Math.cos(Math.toRadians(angle)) * speed,Math.sin(Math.toRadians(angle)) * speed);
		init();
		Main.getPanel().addObject(this);
	}

	@Override
	public void nextTick(float deltaTime) {
		getBoundingBox().moveX(velocity.x * deltaTime);
		getBoundingBox().moveY(velocity.y * deltaTime);

		spriteHandler.nextTick(deltaTime);

		timer += deltaTime;
		List<Entity> entities = Main.serverHandler.getEntities();
		for(Entity entity : entities){
			if(entity.getBoundingBox() != null && Intersections.AABBIntersectsAABB(entity.getBoundingBox(), boundingBox) && !damagedEntities.contains(entity.getId()) &&
					DamageSource.doesDamageApply(entity, source)){
				float d = damage;
				if(shooter instanceof Player p){
					float f = 1;
					for (AbilityTreeNode node : p.abilityTree.getNodes()) {
						if(node.isOwned()){
							Tuple<Float, Boolean> t = node.event.dealProjectileDamage(p, entity);
							if(t.getK()) {
								f = 0;
								break;
							}
							f += t.getV();
						}
					}
					if(f == 0) continue;
					d *= f;
				}
				entity.causeDamage(shooter, d);
				damagedEntities.add(entity.getId());
				lst.add(new Vector2f(getBoundingBox().min.getIntX(),getBoundingBox().min.getIntY()));
			}
		}
		if(timer >= duration){
			Main.serverHandler.removeDamageBox(this);
			Main.getPanel().removeObject(this);
		}
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		spriteHandler.drawCentered(g,boundingBox.getMiddle().getIntX()+offsetX,boundingBox.getMiddle().getIntY()+offsetY);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.FOREGROUND;
	}

	@Override
	public String getId() {
		return id;
	}

	public Vector2f getVelocity() {
		return velocity;
	}

	private static AABB generateBox(String key, Vector2f pos, float angle){
		AnimatedSpriteHandler handler = SpriteUtils.getAnimatedSpriteHandler(key);
		float w = handler.getFrameWidth();
		float h = handler.getFrameHeight();
		double sin = Math.abs(Math.sin(Math.toRadians(angle)));
		double cos = Math.abs(Math.cos(Math.toRadians(angle)));
		double wn = h * sin + w * cos;
		double hn = w * sin + h * cos;
		return new AABB(pos.x - wn/2, pos.y - hn/2, pos.x + wn/2, pos.y + hn/2);
	}
}
