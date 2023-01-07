package com.notlord.game.client;

import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.Vector2f;

import java.awt.*;

public class ProjectileAnimEntity extends Entity {
	private final float duration;
	private final Vector2f velocity;
	public ProjectileAnimEntity(String key, float x, float y, float angle, float duration, Vector2f velocity) {
		super(x, y, 1, key);
		this.spriteHandler.setRotation(angle);
		this.duration = duration;
		this.velocity = velocity;
	}
	private float timer = 0;
	@Override
	public void nextAnimationTick(float dt) {
		setX(getX()+(velocity.x*dt));
		setY(getY()+(velocity.y*dt));
		spriteHandler.nextTick(dt);
		timer += dt;
		if(timer >= duration){
			Main.clientHandler.removeEntity(this);
		}
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		spriteHandler.drawCentered(g,getIntX()+offsetX,getIntY()+offsetY);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.FOREGROUND;
	}
}
