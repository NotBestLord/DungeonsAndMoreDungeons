package com.notlord.networking.packets;

public class AddToAbilityTreePacket {
	private String key;

	public AddToAbilityTreePacket(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
