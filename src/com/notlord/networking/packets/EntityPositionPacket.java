package com.notlord.networking.packets;

import com.notlord.math.Vector2f;

public class EntityPositionPacket {
	private String id;
	private Vector2f position;

	public EntityPositionPacket(String id, Vector2f position) {
		this.id = id;
		this.position = position;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vector2f getPosition() {
		return position;
	}

	public void setPosition(Vector2f position) {
		this.position = position;
	}
}
