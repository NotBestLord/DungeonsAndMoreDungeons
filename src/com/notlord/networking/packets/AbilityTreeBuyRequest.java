package com.notlord.networking.packets;

public class AbilityTreeBuyRequest {
	private String key;
	private int x,y;

	public AbilityTreeBuyRequest(String key, int x, int y) {
		this.key = key;
		this.x = x;
		this.y = y;
	}

	public String getKey() {
		return key;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
