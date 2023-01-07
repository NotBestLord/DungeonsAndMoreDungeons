package com.notlord.game.stats;

import com.notlord.game.Entity;

import java.util.function.Consumer;

public class Effect {
	public final Consumer<Entity> effectStartRunnable;
	public final Consumer<Entity> effectEndRunnable;
	public final float duration;

	public Effect(Consumer<Entity> effectStartRunnable, Consumer<Entity> effectEndRunnable, float duration) {
		this.effectStartRunnable = effectStartRunnable;
		this.effectEndRunnable = effectEndRunnable;
		this.duration = duration;
	}
}
