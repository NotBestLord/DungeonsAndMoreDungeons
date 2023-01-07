package com.notlord.game.server;

public enum DamageSource {
	PLAYER, ENEMY, WORLD;

	public static boolean doesDamageApply(Object o, DamageSource source){
		switch (source){
			case PLAYER -> {return !(o instanceof Player);}
			case ENEMY -> {return !(o instanceof Enemy);}
			case WORLD -> {return true;}
		}
		return false;
	}
}
