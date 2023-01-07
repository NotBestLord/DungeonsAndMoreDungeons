package com.notlord.networking.packets;

public class JoinPacket {
	private String abilityTreeType;

	public JoinPacket(String abilityTreeType) {
		this.abilityTreeType = abilityTreeType;
	}

	public String getAbilityTreeType() {
		return abilityTreeType;
	}
}
