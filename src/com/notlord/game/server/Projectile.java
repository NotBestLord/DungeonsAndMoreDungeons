package com.notlord.game.server;

import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.ID;
import com.notlord.game.abilities.AbilityTreeNode;
import com.notlord.gui.images.Sprite;
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

public class Projectile extends DamageBox implements IRender, ID {
	private final String id = UUID.randomUUID().toString();
	private Sprite sprite;
	private final float angle;
	protected final Vector2f velocity;
	protected List<Vector2f> lst = new ArrayList<>();
	protected final Entity shooter;
	public Projectile(String spriteKey, DamageSource source, Entity shooter, AABB boundingBox, float duration, float angle, float speed, float damage) {
		super(source, damage, boundingBox, duration, shooter);
		sprite = new Sprite(spriteKey);
		sprite.setRotation(angle);
		this.angle = angle;
		this.shooter = shooter;
		velocity = new Vector2f(Math.cos(Math.toRadians(angle)) * speed,Math.sin(Math.toRadians(angle)) * speed);
		init();
		Main.getPanel().addObject(this);
	}

	@Override
	public void nextTick(float deltaTime) {
		getBoundingBox().moveX(velocity.x * deltaTime);
		getBoundingBox().moveY(velocity.y * deltaTime);

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
		int w = getBoundingBox().max.getIntX() - getBoundingBox().min.getIntX();
		int h = getBoundingBox().max.getIntY() - getBoundingBox().min.getIntY();
		int x = getBoundingBox().min.getIntX() + offsetX;
		int y = getBoundingBox().min.getIntY() + offsetY;
		sprite.draw(g,x,y,x + w,y + h);
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
}
