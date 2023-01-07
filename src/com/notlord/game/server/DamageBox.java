package com.notlord.game.server;

import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.ITick;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Intersections;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DamageBox implements ITick, IRender {
	protected final DamageSource source;
	protected final List<String> damagedEntities = new ArrayList<>();
	protected final float damage;
	protected final AABB boundingBox;
	protected final float duration;
	protected final Entity origin;
	public DamageBox(DamageSource source, float damage, AABB boundingBox, float duration, Entity origin) {
		this.source = source;
		this.damage = damage;
		this.boundingBox = boundingBox;
		this.duration = duration;
		this.origin = origin;
	}

	protected void init(){
		Main.serverHandler.addDamageBox(this);
	}

	protected float timer = 0;
	@Override
	public void nextTick(float deltaTime) {
		timer += deltaTime;
		List<Entity> entities = Main.serverHandler.getEntities();
		for(Entity entity : entities){
			if(entity.getBoundingBox() != null && Intersections.AABBIntersectsAABB(entity.getBoundingBox(), boundingBox) && !damagedEntities.contains(entity.getId()) &&
					DamageSource.doesDamageApply(entity, source)){
				entity.causeDamage(origin, damage);
				damagedEntities.add(entity.getId());
			}
		}
		if(timer >= duration){
			Main.serverHandler.removeDamageBox(this);
		}
	}

	protected AABB getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.setColor(Color.pink);
		g.drawRect(boundingBox.min.getIntX()+offsetX, boundingBox.min.getIntY() + offsetY, boundingBox.getWidthInt(), boundingBox.getHeightInt());
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.GUI;
	}
}
