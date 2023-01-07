package com.notlord.networking.packets;

import com.notlord.math.Vector2f;

public class ProjectilePacket {
	private String id;
	private String spriteKey;
	private float duration;
	private float angle;
	private float size;
	private Vector2f position;
	private Vector2f velocity;

	public ProjectilePacket(String id, String spriteKey, float duration, float size,float angle, Vector2f position,Vector2f velocity) {
		this.id = id;
		this.spriteKey = spriteKey;
		this.duration = duration;
		this.size = size;
		this.angle = angle;
		this.position = position;
		this.velocity=velocity;
	}

	public String getId() {
		return id;
	}

	public String getSpriteKey() {
		return spriteKey;
	}

	public float getDuration() {
		return duration;
	}

	public float getSize() {
		return size;
	}

	public float getAngle() {
		return angle;
	}

	public Vector2f getPosition() {
		return position;
	}

	public Vector2f getVelocity() {
		return velocity;
	}
}
