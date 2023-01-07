package com.notlord.networking.packets;

public class InputPacket {
	private String key;
	/**
	 * 0 = pressed, 1 = released
	 */
	private int state;

	/**
	 * @param key key
	 * @param state 0=pressed,1=released
	 */
	public InputPacket(String key, int state) {
		this.key = key;
		this.state = state;
	}

	public String getKey() {
		return key;
	}

	public int getState() {
		return state;
	}
}
