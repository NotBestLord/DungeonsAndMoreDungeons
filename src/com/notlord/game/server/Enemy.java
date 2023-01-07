package com.notlord.game.server;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.ai.EnemyType;
import com.notlord.game.ai.EntityAI;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.Sprite;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.AnimationPacket;
import com.notlord.utils.Tuple;

import java.awt.*;

public class Enemy extends Entity {
	private final Sprite hud_empty = new Sprite("gui_hud_enemy_empty");
	private final Sprite hud_hp = new Sprite("gui_hud_enemy_health_bar");
	private final Vector2f size;
	public int stunned = 0;
	public boolean attacking = false;
	private final EntityAI ai;
	public Enemy(float x, float y, float spriteMult, String key, AABB room) {
		super(x, y, spriteMult, key);
		this.size = new Vector2f(19f,15f);
		getStatHandler().setStat(Stat.HEALTH, 100);
		getStatHandler().setStat(Stat.MAX_HEALTH, 100);
		EntityAI ai;
		try {
			ai = EnemyType.valueOf(key).generateAi(room, EnemyType.valueOf(key).getBaseStats());
		}
		catch (Exception e){
			ai = null;
			System.out.println("Failed to load ai of EnemyType:"+key);
			e.printStackTrace();
		}
		this.ai = ai;
		float avgLvl = 0;
		for (Player player : Main.serverHandler.getAllPlayers()) {
			avgLvl += player.getStat(Stat.LEVEL);
		}
		avgLvl /= Main.serverHandler.getAllPlayers().size();
		if(Math.random() > 0.5){
			getStatHandler().setStat(Stat.LEVEL, ((int) avgLvl) + 1);
		}
		else{
			getStatHandler().setStat(Stat.LEVEL, ((int) avgLvl));
		}
		getStatHandler().setStat(Stat.EXP,
				8 * (float) ((Constants.PLAYER_BASE_EXP_REQ * Math.pow(2, getStatHandler().getStat(Stat.LEVEL)-1)) / (10 + 5 * (getStatHandler().getStat(Stat.LEVEL) - 1))));

	}


	@Override
	public void causeDamage(Entity damager, float damage) {
		if(!isAlive()) return;
		if(ai != null && !ai.damageEvent(this, damager)) return;
		super.causeDamage(damager, damage);
		if(getStatHandler().getStat(Stat.HEALTH) <= 0){
			if(damager instanceof Player player){
				player.addStat(Stat.EXP, getStatHandler().getStat(Stat.EXP));
				if(player.getStat(Stat.EXP) >= player.getStat(Stat.MAX_EXP)){
					player.addStat(Stat.EXP, -player.getStat(Stat.MAX_EXP));
					player.setStat(Stat.MAX_EXP, player.getStat(Stat.MAX_EXP) * 2);
					player.addStat(Stat.LEVEL, 1);
					player.addStat(Stat.AP, 5);
				}
			}
		}
		else{
			stunned++;
			spriteHandler.setCurrentAnimationWFlip("hit");
			Main.serverHandler.server.sendAll(new AnimationPacket(id,"hit",2));
			Main.scheduler.scheduleDelay(() -> stunned--, spriteHandler.getAnimationLengthInSeconds());
		}
	}

	@Override
	public void nextTick(float deltaTime) {
		super.nextTick(deltaTime);
		if(isAlive() && ai != null){
			ai.nextTick(this,deltaTime);
		}
	}

	@Override
	public void handleDeath() {
		setAlive(false);
		spriteHandler.setCurrentAnimationWFlip("death");
		Main.serverHandler.server.sendAll(new AnimationPacket(id,"death",2));
		Main.scheduler.scheduleDelay(() -> {
			Main.serverHandler.getCurrentRoom().enemies--;
			if(Main.serverHandler.getCurrentRoom().enemies < 0) {
				Main.serverHandler.getCurrentRoom().enemies = 0;
			}
			Main.serverHandler.removeEntity(this);
		}, spriteHandler.getAnimationLengthInSeconds() * 0.9f);
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		super.render(g, offsetX, offsetY);
		if(getStatHandler().getStat(Stat.MAX_HEALTH) != 0 && getStatHandler().getStat(Stat.HEALTH) / getStatHandler().getStat(Stat.MAX_HEALTH) != 1){
			g.drawImage(hud_empty.getImage(),
					getIntX() + offsetX - (hud_hp.getImage().getWidth()/2),
					(int) (getIntY() + offsetY - (spriteHandler.getFrameHeight() * spriteMult / 2)), null);

			int w = (int) (Math.min(getStatHandler().getStat(Stat.HEALTH) / getStatHandler().getStat(Stat.MAX_HEALTH), 1f) *
					(hud_hp.getImage().getWidth()-10));
			w = Math.min(Math.max(w,0), hud_hp.getImage().getWidth());
			g.drawImage(hud_hp.getImage().getSubimage(0,0,5+w,hud_hp.getImage().getHeight()),
					getIntX() + offsetX - (hud_hp.getImage().getWidth()/2),
					(int) (getIntY() + offsetY - (spriteHandler.getFrameHeight() * spriteMult / 2)), null);
		}
	}

	@Override
	public AABB getBoundingBox() {
		AABB aabb = new AABB(position.x,position.y,position.x+(size.x*spriteMult), position.y+(size.y*spriteMult));
		aabb.moveX(-size.x*spriteMult/2);
		aabb.moveY(-size.y*spriteMult);
		return aabb;
	}


	public Tuple<AABB, Float> getTargetBox() {
		AABB aabb = new AABB(position.x,position.y,position.x+(size.x*spriteMult), position.y+(size.y*spriteMult));
		aabb.moveX(-size.x*spriteMult/2);
		aabb.moveY(-size.y*spriteMult);
		float h = aabb.getHeight();
		aabb.min.y += h/8;
		aabb.max.y -= h/8;
		float w = aabb.getWidth();
		aabb.min.x += w/6;
		aabb.max.x -= w/6;
		return new Tuple<>(aabb,size.x*spriteMult/2);
	}

}
