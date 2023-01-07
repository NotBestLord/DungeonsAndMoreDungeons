package com.notlord.game.ai;

import com.notlord.math.AABB;

import java.util.HashMap;
import java.util.Map;

public enum EnemyType {
	slime_pink(new EnemyAIMelee(null, 256, 256), Map.of("SPEED", 10f)),

	slime_blue(new EnemyAIRanged(256, 312, 120,208, null, 2, "slime_blue_projectile"),
			Map.of("PROJ_DURATION", 4f, "PROJ_SIZE",16f,"PROJ_SPEED", 96f, "PROJ_DAMAGE", 20f,"SPEED", 10f, "ATTACK_CD",2f)),

	slime_brown(new EnemyAIRanged(256, 312, 80,100, null, 1.2f, "slime_brown_projectile"),
			Map.of("PROJ_DURATION", 4f, "PROJ_SIZE",16f,"PROJ_SPEED", 128f, "PROJ_DAMAGE", 20f,"SPEED", 2f,"ATTACK_CD",2f))
	;

	private final EntityAI aiTemplate;
	private final Map<String, Float> baseStats;


	EnemyType(EntityAI aiTemplate, Map<String, Float> baseStats) {
		this.aiTemplate = aiTemplate;
		this.baseStats = baseStats;
	}

	public EntityAI generateAi(AABB area, Map<String, Float> stats){
		return aiTemplate.duplicate(area, stats);
	}

	public Map<String, Float> getBaseStats() {
		return new HashMap<>(baseStats);
	}
}
