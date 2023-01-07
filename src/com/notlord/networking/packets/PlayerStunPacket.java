package com.notlord.networking.packets;

public class PlayerStunPacket {
	private boolean stunned;

	public PlayerStunPacket(boolean stunned) {
		this.stunned = stunned;
	}

	public boolean isStunned() {
		return stunned;
	}
}
