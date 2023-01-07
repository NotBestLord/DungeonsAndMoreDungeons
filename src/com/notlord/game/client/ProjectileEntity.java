package com.notlord.game.client;

import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.Vector2f;

import java.awt.*;

public class ProjectileEntity extends Entity {
	private final float size;
	private final Sprite sprite;
	private final float duration;
	private final Vector2f velocity;
	public ProjectileEntity(float x, float y, float size, float angle, float duration, String sprite, Vector2f velocity) {
		super(x, y, 1);
		this.sprite = new Sprite(sprite);
		this.sprite.setRotation(angle);
		this.duration = duration;
		this.size = size;
		this.velocity = velocity;
	}
	private float timer = 0;
	@Override
	public void nextAnimationTick(float dt) {
		setX(getX()+(velocity.x*dt));
		setY(getY()+(velocity.y*dt));
		timer += dt;
		if(timer >= duration){
			Main.clientHandler.removeEntity(this);
		}
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		sprite.draw(g,
				(int) (position.x - size) + offsetX,
				(int) (position.y - size) + offsetY,
				(int) (position.x + size) + offsetX,
				(int) (position.y + size) + offsetY);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.FOREGROUND;
	}
}
