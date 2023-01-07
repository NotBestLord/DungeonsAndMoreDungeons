package com.notlord.game;

import com.notlord.Main;
import com.notlord.gui.images.IAnimationTick;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;

import java.awt.*;

public class Particle  implements IPosition, IRender, IAnimationTick {
	private final Sprite sprite;
	private final Vector2f position, size, velocity;
	private final float lifeTime;

	public Particle(String spriteKey, Vector2f position, Vector2f size, float lifeTime, float velocityMul, float angle) {
		this.sprite = new Sprite(spriteKey);
		this.sprite.setRotation(angle);
		this.position = position;
		this.size = size;
		this.lifeTime = lifeTime;
		this.velocity = new Vector2f(angle).mul(velocityMul);
	}

	@Override
	public int getIntY() {
		return position.getIntY();
	}

	@Override
	public int getIntX() {
		return position.getIntX();
	}

	@Override
	public float getX() {
		return position.x;
	}

	@Override
	public float getY() {
		return position.y;
	}

	@Override
	public void setX(float x) {
		position.x = x;
	}

	@Override
	public void setY(float y) {
		position.y = y;
	}

	@Override
	public AABB getBoundingBox() {
		return new AABB(position.x, position.y, position.x+size.x,position.y+size.y);
	}

	@Override
	public Vector2f getPosition() {
		return position;
	}

	private float timer = 0;
	@Override
	public void nextAnimationTick(float deltaTime) {
		timer+=deltaTime;
		position.add(new Vector2f(velocity).mul(deltaTime));
		if(timer >= lifeTime){
			Main.getPanel().removeObject(this);
		}
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		sprite.draw(g,(int) (getIntX()-size.x/2)+offsetX, (int) (getIntY()-size.x/2)+offsetY);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.PARTICLE;
	}


}
