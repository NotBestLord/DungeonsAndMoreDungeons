package com.notlord.networking.packets;

import com.notlord.game.stats.Stat;

public class StatUpdatePacket {
	private Stat stat;
	private float n;

	public StatUpdatePacket(Stat stat, float n) {
		this.stat = stat;
		this.n = n;
	}

	public Stat getStat() {
		return stat;
	}

	public float getN() {
		return n;
	}
}
