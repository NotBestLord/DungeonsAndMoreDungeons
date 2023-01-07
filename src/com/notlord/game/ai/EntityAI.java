package com.notlord.game.ai;

import com.notlord.game.Entity;
import com.notlord.game.server.Enemy;
import com.notlord.math.AABB;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public abstract class EntityAI {
	protected final AABB field;
	private final Map<String, Object> objects = new HashMap<>();
	protected final List<BiConsumer<Enemy,Float>> actions = new CopyOnWriteArrayList<>();
	protected final Map<String, Float> stats = new ConcurrentHashMap<>();
	public EntityAI(AABB field) {
		this.field = field;
	}

	public void nextTick(Enemy self, float tick) {
		actions.forEach(c -> c.accept(self,tick));
		Float w;
		if((w=get("Wait")) != null){
			put("Wait", w - tick);
			if((float) get("Wait") < 0f){
				remove("Wait");
			}
		}
	}

	public EntityAI duplicate(AABB field, Map<String, Float> stats){
		return null;
	}

	public void render(Enemy self, Graphics g, int offsetX, int offsetY) {}

	public void put(String k, Object v){
		objects.put(k,v);
	}

	public <T> T get(String k){
		return (objects.containsKey(k) ? (T) objects.get(k) : null);
	}

	public void remove(String k){
		objects.remove(k);
	}

	public boolean damageEvent(Enemy self, Entity damager){
		return true;
	}

}
