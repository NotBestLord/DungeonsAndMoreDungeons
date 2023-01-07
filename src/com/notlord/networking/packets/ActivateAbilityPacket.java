package com.notlord.networking.packets;

import com.notlord.game.abilities.Ability;

public class ActivateAbilityPacket {
	private Ability ability;
	private Object[] args;

	public ActivateAbilityPacket(Ability ability, Object[] args) {
		this.ability = ability;
		this.args = args;
	}

	public Ability getAbility() {
		return ability;
	}

	public Object[] getArgs() {
		return args;
	}
}
