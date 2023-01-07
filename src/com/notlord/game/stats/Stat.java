package com.notlord.game.stats;

public enum Stat {
	HEALTH,
	MAX_HEALTH,
	HEALTH_REGEN,
	MANA,
	MAX_MANA,
	MANA_REGEN,
	DEFENSE,
	ATTACK,
	SPEED,
	DASH_SPEED,
	AGGRO,
	EXP,
	MAX_EXP,
	LEVEL,
	AP,
	CURRENCY;


	public static float calculateSpeed(float spd){
		return (float) (200 - (800 / Math.sqrt(Math.abs(spd) + 16))) + (spd >= 0 ? 150 : 0);
	}

	public static float calculateDefense(float def){
		return 100 - (float) (Math.signum(def) * (100 - (300 / Math.sqrt(Math.abs(def) + 9))));
	}

	public static float calculateAttack(float atk){
		if(atk > 50){
			return (float) (4 * Math.sqrt(Math.pow(atk - 50, 1.5)) + 125);
		}
		else{
			return (2 * atk) + 25;
		}
	}

	public static float calculateCurrencyDrop(float level){
		return (float) (Math.random() * Math.pow(3, Math.sqrt(level)));
	}
}
