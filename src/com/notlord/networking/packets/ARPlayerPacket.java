package com.notlord.networking.packets;

import com.notlord.game.Entity;

public class ARPlayerPacket extends AREntityPacket{
	/**
	 * Sent to owner of player data
	 * @param save  entity data
	 * @param state 0=add,1=remove
	 */
	public ARPlayerPacket(Entity.EntitySave save, int state) {
		super(save, state);
	}
}
