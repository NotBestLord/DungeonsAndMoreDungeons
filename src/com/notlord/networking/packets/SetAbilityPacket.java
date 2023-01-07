package com.notlord.networking.packets;

import com.notlord.game.abilities.Ability;

public class SetAbilityPacket {
	private final Ability ability;
	private final int i;

	public SetAbilityPacket(Ability ability, int i) {
		this.ability = ability;
		this.i = i;
	}

	public Ability getAbility() {
		return ability;
	}

	public int getI() {
		return i;
	}
}
