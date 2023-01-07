package com.notlord.game.stats;

import java.util.HashMap;
import java.util.Map;

public class StatHandler {
	private final Map<Stat, Float> stats = new HashMap<>();
	public StatHandler(){
		for (Stat value : Stat.values()) {
			stats.put(value, 0f);
		}
	}

	public float getStat(Stat stat){
		return stats.get(stat);
	}

	public float addStat(Stat stat, float n){
		stats.put(stat, stats.get(stat) + n);
		return stats.get(stat);
	}

	public void setStat(Stat stat, float n){
		stats.put(stat, n);
	}

	public int getStatAsInt(Stat stat){
		return stats.get(stat).intValue();
	}
}
