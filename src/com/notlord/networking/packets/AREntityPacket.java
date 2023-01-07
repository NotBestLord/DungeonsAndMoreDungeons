package com.notlord.networking.packets;

import com.notlord.game.Entity;

public class AREntityPacket {
	private Entity.EntitySave save;
	private int state;

	/**
	 *
	 * @param save entity data
	 * @param state 0=add,1=remove
	 */
	public AREntityPacket(Entity.EntitySave save, int state) {
		this.save = save;
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public Entity.EntitySave getSave() {
		return save;
	}
}
