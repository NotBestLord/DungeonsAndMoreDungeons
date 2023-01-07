package com.notlord.networking.packets;

public class RoomGeneratePacket {
	private String key;
	private int n;

	public RoomGeneratePacket(String key, int n) {
		this.key = key;
		this.n = n;
	}

	public String getKey() {
		return key;
	}

	public int getN() {
		return n;
	}
}
