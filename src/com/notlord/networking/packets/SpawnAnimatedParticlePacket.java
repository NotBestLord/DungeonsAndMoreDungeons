package com.notlord.networking.packets;

import com.notlord.math.Vector2f;

public class SpawnAnimatedParticlePacket {
	private String spriteKey;
	private String animationState;
	private Vector2f position;
	private Vector2f size;
	private float lifeTime;
	private float angle;

	public SpawnAnimatedParticlePacket(String spriteKey, String animationState, Vector2f position, Vector2f size, float lifeTime, float angle) {
		this.spriteKey = spriteKey;
		this.position = position;
		this.size = size;
		this.lifeTime = lifeTime;
		this.angle = angle;
		this.animationState=animationState;
	}

	public String getSpriteKey() {
		return spriteKey;
	}

	public Vector2f getPosition() {
		return position;
	}

	public Vector2f getSize() {
		return size;
	}

	public float getLifeTime() {
		return lifeTime;
	}

	public float getAngle() {
		return angle;
	}

	public void setSpriteKey(String spriteKey) {
		this.spriteKey = spriteKey;
	}

	public void setPosition(Vector2f position) {
		this.position = position;
	}

	public void setSize(Vector2f size) {
		this.size = size;
	}

	public void setLifeTime(float lifeTime) {
		this.lifeTime = lifeTime;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public String getAnimationState() {
		return animationState;
	}

	public void setAnimationState(String animationState) {
		this.animationState = animationState;
	}
}
