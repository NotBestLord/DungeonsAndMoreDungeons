package com.notlord.networking.packets;

import com.notlord.math.Vector2f;

public class SpawnParticlePacket {
	private String spriteKey;
	private Vector2f position;
	private Vector2f size;
	private float lifeTime, angle, velocityMul;

	public SpawnParticlePacket(String spriteKey, Vector2f position, Vector2f size, float lifeTime, float velocityMul, float angle) {
		this.spriteKey = spriteKey;
		this.position = position;
		this.size = size;
		this.lifeTime = lifeTime;
		this.angle = angle;
		this.velocityMul = velocityMul;
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

	public float getVelocityMul() {
		return velocityMul;
	}
}
