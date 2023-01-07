package com.notlord.game;

import com.notlord.Main;
import com.notlord.gui.images.AnimatedSpriteHandler;
import com.notlord.gui.images.SpriteUtils;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;

import java.awt.*;

public class ParticleAnim implements ITick, IPosition, IRender {
	private final AnimatedSpriteHandler spriteHandler;
	private final Vector2f position, size;
	private final float lifeTime;

	public ParticleAnim(String spriteKey, String animationState, Vector2f position, Vector2f size, float lifeTime, float angle) {
		this.spriteHandler = SpriteUtils.getAnimatedSpriteHandler(spriteKey);
		spriteHandler.setRotation(angle);
		spriteHandler.setCurrentAnimation(animationState);
		this.position = position;
		this.size = size;
		this.lifeTime = lifeTime;
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
	public void nextTick(float deltaTime) {
		spriteHandler.nextTick(deltaTime);
		timer+=deltaTime;
		if(timer >= lifeTime){
			Main.getPanel().removeObject(this);
		}
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		spriteHandler.draw(g,(int) (getIntX()-size.x/2)+offsetX, (int) (getIntY()-size.x/2)+offsetY);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.PARTICLE;
	}
}
