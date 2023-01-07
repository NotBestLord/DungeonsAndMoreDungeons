package com.notlord.networking.packets;

public class EntityVisibilityPacket {
	private String id;
	private boolean visible;

	public EntityVisibilityPacket(String id, boolean visible) {
		this.id = id;
		this.visible = visible;
	}

	public String getId() {
		return id;
	}

	public boolean isVisible() {
		return visible;
	}
}
