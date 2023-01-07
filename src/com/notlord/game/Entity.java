package com.notlord.game;

import com.notlord.game.stats.Effect;
import com.notlord.game.stats.Stat;
import com.notlord.game.stats.StatHandler;
import com.notlord.gui.images.AnimatedSpriteHandler;
import com.notlord.gui.images.IAnimationTick;
import com.notlord.gui.images.SpriteUtils;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;

import java.awt.*;
import java.util.*;

public class Entity implements IPosition, ITick, ID, IRender, IAnimationTick {
	public final AnimatedSpriteHandler spriteHandler;
	protected final Vector2f position;
	protected final float spriteMult;
	private boolean alive = true;
	public final String id;
	private final String sprite;
	private final StatHandler statHandler = new StatHandler();
	private final Map<String, Float> flags = new HashMap<>();
	private final Map<Effect, Float> effects = new LinkedHashMap<>();
	public Entity(float x, float y, float spriteMult) {
		position = new Vector2f(x,y);
		this.spriteMult = spriteMult;
		sprite = null;
		id = UUID.randomUUID().toString();
		spriteHandler = null;
	}

	public Entity(float x, float y, float spriteMult, String sprite) {
		position = new Vector2f(x,y);
		this.spriteMult = spriteMult;
		this.sprite = sprite;
		id = UUID.randomUUID().toString();
		spriteHandler = SpriteUtils.getAnimatedSpriteHandler(sprite);
	}

	protected Entity(Vector2f position, float spriteMult, String sprite, String id) {
		this.position = position;
		this.spriteMult = spriteMult;
		this.sprite = sprite;
		if(sprite != null) spriteHandler = SpriteUtils.getAnimatedSpriteHandler(sprite);
		else spriteHandler = null;
		this.id = id;
	}

	public void causeDamage(Entity damager, float damage){
		statHandler.addStat(Stat.HEALTH, -(damage * Stat.calculateDefense(statHandler.getStat(Stat.DEFENSE)) / 100));
		if(statHandler.getStat(Stat.HEALTH) <= 0){
			handleDeath();
		}
	}

	public void handleDeath(){}

	@Override
	public int getIntY() {
		return position.getIntY();
	}

	@Override
	public int getIntX() {
		return position.getIntX();
	}

	@Override
	public float getX() {
		return position.x;
	}

	@Override
	public float getY() {
		return position.y;
	}

	@Override
	public void setX(float x) {
		position.x = x;
	}

	@Override
	public void setY(float y) {
		position.y = y;
	}

	@Override
	public AABB getBoundingBox() {
		return null;
	}

	@Override
	public Vector2f getPosition() {
		return position;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public StatHandler getStatHandler() {
		return statHandler;
	}

	@Override
	public void nextTick(float deltaTime) {
		for (Effect effect : new ArrayList<>(effects.keySet())) {
			effects.put(effect, effects.get(effect) + deltaTime);
			if(effects.get(effect) >= effect.duration){
				if(effect.effectEndRunnable != null)
					effect.effectEndRunnable.accept(this);
				effects.remove(effect);
			}
		}
	}

	public void applyEffect(Effect effect){
		if(effect.effectStartRunnable != null)
			effect.effectStartRunnable.accept(this);
		effects.put(effect, 0f);
	}

	public EntitySave save(){
		return new EntitySave(sprite,position,spriteMult,id);
	}

	@Override
	public String getId() {
		return id;
	}

	protected boolean visibility = true;

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	@Override
	public void render(Graphics g, int offsetX,int offsetY) {
		if(visibility) {
			spriteHandler.draw(g,
					(int) (position.getIntX() - (spriteHandler.getFrameWidth() * spriteMult / 2)) + offsetX,
					(int) (position.getIntY() - (spriteHandler.getFrameHeight() * spriteMult)) + offsetY,
					(int) (position.getIntX() + (spriteHandler.getFrameWidth() * spriteMult / 2)) + offsetX,
					position.getIntY() + offsetY);
		}
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.NORMAL;
	}

	@Override
	public void nextAnimationTick(float dt) {
		if(spriteHandler != null) spriteHandler.nextTick(dt);
	}


	public void setFlag(String key, int value) {
		flags.put(key, (float) value);
	}

	public void setFlag(String key, float value) {
		flags.put(key, value);
	}

	public int getFlagI(String key){
		return flags.getOrDefault(key, -1f).intValue();
	}

	public float getFlagF(String key){
		return flags.getOrDefault(key, -1f);
	}

	public boolean hasFlag(String key) {
		return flags.containsKey(key);
	}

	public void removeFlag(String key){
		flags.remove(key);
	}

	public static class EntitySave implements ID{
		private String spriteKey;
		private Vector2f position;
		private float spriteMult;
		private String id;

		public EntitySave(String spriteKey, Vector2f position, float spriteMult, String id) {
			this.spriteKey = spriteKey;
			this.spriteMult = spriteMult;
			this.position = position;
			this.id = id;
		}

		public Entity load(){
			return new Entity(position, spriteMult, spriteKey, id);
		}

		public Entity load(String spriteKey){
			return new Entity(position, spriteMult, spriteKey, id);
		}

		public String getSpriteKey() {
			return spriteKey;
		}

		public void setSpriteKey(String spriteKey) {
			this.spriteKey = spriteKey;
		}

		public Vector2f getPosition() {
			return position;
		}

		public void setPosition(Vector2f position) {
			this.position = position;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public float getSpriteMult() {
			return spriteMult;
		}

		public void setSpriteMult(int spriteMult) {
			this.spriteMult = spriteMult;
		}
	}
}
