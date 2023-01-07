package com.notlord.networking.packets;

public class ExitPercentPacket {
	private int stateChange;
	private float percent;

	public ExitPercentPacket() {
		this.stateChange = 1;
		percent = 0;
	}
	public ExitPercentPacket(float percent) {
		this.stateChange = 0;
		this.percent = percent;
	}

	public int getStateChange() {
		return stateChange;
	}

	public float getPercent() {
		return percent;
	}
}
