package com.notlord.networking.packets;

public class MouseInputPacket {
	private int x,y,button;

	public MouseInputPacket(int x, int y,int button) {
		this.x = x;
		this.y = y;
		this.button = button;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getButton() {
		return button;
	}
}
