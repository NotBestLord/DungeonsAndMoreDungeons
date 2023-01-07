package com.notlord.game.abilities;

import com.notlord.Main;
import com.notlord.Scheduler;
import com.notlord.Window;
import com.notlord.game.Entity;
import com.notlord.game.Particle;
import com.notlord.game.client.ClientPlayer;
import com.notlord.game.server.DamageSource;
import com.notlord.game.server.Enemy;
import com.notlord.game.server.Player;
import com.notlord.game.server.ProjectileAnim;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.Sprite;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public enum Ability {
	MAGE_MANA_BLAST(Map.ofEntries(
			Map.entry("MANA_BLAST_DAMAGE",15f),
			Map.entry("MANA_BLAST_COST",20f),
			Map.entry("MANA_BLAST_DURATION",5f),
			Map.entry("MANA_BLAST_SPEED",64f)
	), args ->
	{
		double x;
		double y;
		try {
			x = (float) args.args[0];
			y = (float) args.args[1];
		}
		catch (Exception ignored){
			x = (double) args.args[0];
			y = (double) args.args[1];
		}
		float dmg = args.player.getFlagF("MANA_BLAST_DAMAGE");
		float cost = args.player.getFlagF("MANA_BLAST_COST");
		float dur = args.player.getFlagF("MANA_BLAST_DURATION");
		float spd = args.player.getFlagF("MANA_BLAST_SPEED");
		if(args.player.getStat(Stat.MANA) < cost) return;
		args.player.addStat(Stat.MANA, -cost);
		float sx=args.player.getBoundingBox().getMiddle().x,sy=args.player.getBoundingBox().min.y;
		float angle = (float) Math.toDegrees(Math.atan2(y - sy, x - sx));
		if(angle < 0){
			angle += 360;
		}
		ProjectileAnim projectile = new ProjectileAnim("projectile_arrow", DamageSource.PLAYER, args.player, new Vector2f(sx,sy),dur,angle,spd,dmg);
		Main.serverHandler.server.sendAll(new ProjectileAnimPacket(projectile.getId(),
				"projectile_blue", dur, angle, new Vector2f(sx, sy), projectile.getVelocity()));
	}, "mouse","icon_mage_mana_blast"),
	MAGE_BLINK(Map.ofEntries(
			Map.entry("BLINK_DURATION",0.5f),
			Map.entry("BLINK_DISTANCE",128f),
			Map.entry("BLINK_COST",10f),
			Map.entry("BLINK_CD",0f),
			Map.entry("BLINK_DAMAGE_RADIUS",128f),
			Map.entry("BLINK_DAMAGE",0f)
	), args -> {
		if(args.player.getFlagF("BLINK_CD") != 0) return;
		args.player.setFlag("BLINK_CD", 1);
		float dur = args.player.getFlagF("BLINK_DURATION");
		float dist = args.player.getFlagF("BLINK_DISTANCE");
		float cost = args.player.getFlagF("BLINK_COST");
		float rad = args.player.getFlagF("BLINK_DAMAGE_RADIUS");
		float dmg = args.player.getFlagF("BLINK_DAMAGE");
		if(args.player.getStat(Stat.MANA) < cost) {
			args.player.setFlag("BLINK_CD", 0);
			return;
		}
		args.player.addStat(Stat.MANA, -cost);
		Vector2f dir = new Vector2f();
		for(String key : new ArrayList<>(args.player.getHeldDownKeys())) {
			switch (key) {
				case "move_up" -> dir.y = -1;
				case "move_down" -> dir.y = 1;
				case "move_left" -> dir.x = -1;
				case "move_right" -> dir.x = 1;
			}
		}
		boolean b = true;
		AABB aabb = args.player.getPhysicsBoundingBox();
		for (int i = 1; i <= ((int) dist); i++) {
			if(Main.serverHandler.getCurrentRoom().isOutOfBounds(aabb.moveX(dir.x).moveY(dir.y))){
				dir.mul(i-1);
				b = false;
				break;
			}

		}
		if(b) dir.mul(dist);
		args.player.dashing = true;
		if(args.player.getClientInstance() != null) args.player.getClientInstance().send(new PlayerStunPacket(true));
		args.player.setVisibility(false);
		Main.serverHandler.server.sendAll(new EntityVisibilityPacket(args.player.id, false));
		for (int i = 0; i < 10; i++) {
			Particle particle = new Particle("particle_cloud", args.player.getBoundingBox().getRandomPoint(),
					new Vector2f(16,16),dur/2f,64f,(float) (Math.random()*360f));
			Main.getPanel().addObject(particle);
			Main.serverHandler.server.sendAll(new SpawnParticlePacket("particle_cloud", args.player.getBoundingBox().getRandomPoint(),
					new Vector2f(16,16),dur/2f,64f,(float) (Math.random()*360f)));
		}
		Main.scheduler.scheduleDelay(() -> {
			args.player.getPosition().add(dir);
			if(args.player.getClientInstance() != null){
				args.player.getClientInstance().send(new EntityPositionPacket(args.player.id,args.player.getPosition()));
			}
			for (int i = 0; i < 10; i++) {
				Particle particle = new Particle("particle_cloud", args.player.getBoundingBox().getRandomPoint(),
						new Vector2f(16,16),dur/2f,64f,(float) (Math.random()*360f));
				Main.getPanel().addObject(particle);
				Main.serverHandler.server.sendAll(new SpawnParticlePacket("particle_cloud", args.player.getBoundingBox().getRandomPoint(),
						new Vector2f(16,16),dur/2f,64f,(float) (Math.random()*360f)));
			}
		}, dur/2f);
		Main.scheduler.scheduleDelay(() -> {
			args.player.dashing = false;
			if(dmg != 0){
				for (Entity e : Main.serverHandler.getEntities()) {
					if(!(e instanceof Player) && args.player.getBoundingBox().getMiddle().distance(e.getBoundingBox().getMiddle()) <= rad){
						e.causeDamage(args.player, dmg);
						for (int i = 0; i < 4; i++) {
							Particle particle = new Particle("particle_damage", e.getBoundingBox().getRandomPoint(),
									new Vector2f(16, 16), 0.4f, 0, 0);
							Main.getPanel().addObject(particle);
							Main.serverHandler.server.sendAll(new SpawnParticlePacket("particle_damage", e.getBoundingBox().getRandomPoint(),
									new Vector2f(16, 16), 0.4f, 0, 0));
						}
					}
				}
			}
			if(args.player.getClientInstance() != null) args.player.getClientInstance().send(new PlayerStunPacket(false));
			args.player.setVisibility(true);
			Main.serverHandler.server.sendAll(new EntityVisibilityPacket(args.player.id, true));
			args.player.setFlag("BLINK_CD", 0);
		}, dur);
	}, "","icon_mage_blink"),
	MAGE_HEAL(Map.ofEntries(
			Map.entry("HEAL_PERCENT",5f),
			Map.entry("HEAL_COST",20f),
			Map.entry("HEAL_RADIUS",64f),
			Map.entry("HEAL_TIMES",3f),
			Map.entry("HEAL_TOTAL_TIME",3f),
			Map.entry("HEAL_DAMAGE_PERCENT",0f),
			Map.entry("HEAL_CD",0f)
			), args -> {
		try{
			if(args.player.getFlagF("HEAL_CD") != 0) return;
			args.player.setFlag("HEAL_CD", 1);
			float per = args.player.getFlagF("HEAL_PERCENT") / 100f;
			float cost = args.player.getFlagF("HEAL_COST");
			float radius = args.player.getFlagF("HEAL_RADIUS");
			float times = args.player.getFlagF("HEAL_TIMES");
			float totalTime = args.player.getFlagF("HEAL_TOTAL_TIME");
			float dmgPer = args.player.getFlagF("HEAL_DAMAGE_PERCENT");
			if(args.player.getStat(Stat.MANA) < cost) {
				args.player.setFlag("HEAL_CD", 0);
				return;
			}
			args.player.addStat(Stat.MANA, -cost);
			Main.scheduler.scheduleRepeatDuration(() -> {
				args.player.addStat(Stat.HEALTH, args.player.getStat(Stat.MAX_HEALTH) * per);
				Vector2f mid = args.player.getBoundingBox().getMiddle();
				for (int i = 0; i < 10; i++) {
					Particle particle = new Particle("particle_heal", args.player.getBoundingBox().getRandomPoint(),
							new Vector2f(16,16),0.4f,0,0);
					Main.getPanel().addObject(particle);
					Main.serverHandler.server.sendAll(new SpawnParticlePacket("particle_heal", args.player.getBoundingBox().getRandomPoint(),
							new Vector2f(16,16),0.4f,0,0));
				}
				for (Entity e : Main.serverHandler.getEntities()) {
					if(e instanceof Player p) {
						if (p != args.player && p.getBoundingBox().getMiddle().distance(mid) <= radius) {
							p.addStat(Stat.HEALTH, p.getStat(Stat.MAX_HEALTH) * per / 2f);
							for (int i = 0; i < 4; i++) {
								Particle particle = new Particle("particle_heal", p.getBoundingBox().getRandomPoint(),
										new Vector2f(16, 16), 0.4f, 0, 0);
								Main.getPanel().addObject(particle);
								Main.serverHandler.server.sendAll(new SpawnParticlePacket("particle_heal", p.getBoundingBox().getRandomPoint(),
										new Vector2f(16, 16), 0.4f, 0, 0));
							}
						}
					}
					else if(e instanceof Enemy n && dmgPer != 0 && n.getBoundingBox().getMiddle().distance(mid) <= radius){
						n.causeDamage(args.player, args.player.getStat(Stat.MAX_HEALTH) * per * dmgPer);
						for (int i = 0; i < 4; i++) {
							Particle particle = new Particle("particle_damage", n.getBoundingBox().getRandomPoint(),
									new Vector2f(16, 16), 0.4f, 0, 0);
							Main.getPanel().addObject(particle);
							Main.serverHandler.server.sendAll(new SpawnParticlePacket("particle_damage", n.getBoundingBox().getRandomPoint(),
									new Vector2f(16, 16), 0.4f, 0, 0));
						}
					}
				}
			},totalTime / times,totalTime);
			Main.scheduler.scheduleDelay(() -> args.player.setFlag("HEAL_CD", 0), totalTime);
		}
		catch (Exception ignored){}
	}, "","icon_mage_heal"),
	MAGE_MANA_CACHE(Map.ofEntries(
			Map.entry("MANA_CACHE_MAX",40f),
			Map.entry("MANA_CACHE_MANA",0f),
			Map.entry("MANA_CACHE_ADD",2f),
			Map.entry("MANA_CACHE_LIMIT",0f),
			Map.entry("MANA_CACHE_PROJ", 0f)
	), args -> {
		float mana = args.player.getFlagF("MANA_CACHE_MANA");
		if(args.player.getStat(Stat.MANA) + mana <= args.player.getStat(Stat.MAX_MANA) || args.player.getFlagF("MANA_CACHE_LIMIT") != 0){
			args.player.setStat(Stat.MANA, args.player.getStat(Stat.MANA) + mana);
			args.player.setFlag("MANA_CACHE_MANA", 0);
		}
		else {
			args.player.setFlag("MANA_CACHE_MANA", mana - (args.player.getStat(Stat.MAX_MANA) - args.player.getStat(Stat.MANA)));
			args.player.setStat(Stat.MANA, args.player.getStat(Stat.MAX_MANA));
		}
	}, "", "icon_mage_mana_cache"),
	MAGE_FIRE_TOTEM(Map.ofEntries(
			Map.entry("FIRE_TOTEM_DURATION",15f),
			Map.entry("FIRE_TOTEM_RANGE",256f),
			Map.entry("FIRE_TOTEM_PROJ_SPEED",96f),
			Map.entry("FIRE_TOTEM_PROJ_DAMAGE",20f),
			Map.entry("FIRE_TOTEM_PROJ_CD",2f),
			Map.entry("FIRE_TOTEM_PROJ_TIMES",1f),
			Map.entry("FIRE_TOTEM_COST",50f),
			Map.entry("FIRE_TOTEM_MAX",1f),
			Map.entry("FIRE_TOTEM",0f)
	), args -> {
		if(args.player.getFlagF("FIRE_TOTEM") >= args.player.getFlagF("FIRE_TOTEM_MAX")) return;
		float dur = args.player.getFlagF("FIRE_TOTEM_DURATION");
		float range = args.player.getFlagF("FIRE_TOTEM_RANGE");
		float spd = args.player.getFlagF("FIRE_TOTEM_PROJ_SPEED");
		float dmg = args.player.getFlagF("FIRE_TOTEM_PROJ_DAMAGE");
		float cd = args.player.getFlagF("FIRE_TOTEM_PROJ_CD");
		float times = args.player.getFlagF("FIRE_TOTEM_PROJ_TIMES");
		float cost = args.player.getFlagF("FIRE_TOTEM_COST");
		if(args.player.getStat(Stat.MANA) < cost) {
			return;
		}
		args.player.addStat(Stat.MANA, -cost);
		args.player.setFlag("FIRE_TOTEM", args.player.getFlagF("FIRE_TOTEM")+1);
		Entity totem = new Entity(args.player.getX(), args.player.getY(), 4,"totem_fire");
		Main.serverHandler.addEntity(totem);
		Vector2f pos = totem.getPosition().clone().add(new Vector2f(0,-totem.spriteHandler.getFrameHeight() * 2f));
		totem.spriteHandler.setCurrentAnimation("spawn");
		Main.scheduler.scheduleSequence(
				totem.spriteHandler.getAnimationLengthInSeconds(),
				new Scheduler.RepeatDurationRunnable(() -> {
					int t = 1;
					for (Entity entity : Main.serverHandler.getEntities()) {
						if(entity instanceof Enemy n && n.getBoundingBox().getMiddle().distance(pos) <= range){
							totem.spriteHandler.setCurrentAnimation("attack");
							totem.spriteHandler.setCurrentAnimationFlipped(pos.x > n.getX());
							float sx=pos.x,sy=pos.y;
							float angle = (float) Math.toDegrees(Math.atan2(n.getY() - sy, n.getX() - sx));
							if(angle < 0){
								angle += 360;
							}
							ProjectileAnim projectile = new ProjectileAnim("projectile_red", DamageSource.PLAYER, args.player, new Vector2f(sx,sy),dur,angle,spd,dmg);
							Main.serverHandler.server.sendAll(new ProjectileAnimPacket(projectile.getId(),
									"projectile_red", dur, angle, new Vector2f(sx, sy), projectile.getVelocity()));
							if((t++) >= times){
								break;
							}
						}
					}
				}, cd, dur),
				(Runnable) () -> totem.spriteHandler.setCurrentAnimationWFlip("death"),
				totem.spriteHandler.getAnimationLengthInSeconds() - 0.075f,
				(Runnable) () -> {
					Main.getPanel().removeObject(totem);
					args.player.setFlag("FIRE_TOTEM", args.player.getFlagF("FIRE_TOTEM") - 1);
				}
		);
	}, "", "icon_mage_fire_totem")
	/*MAGE_MANA_MARK(Map.ofEntries(
			Map.entry("MANA_MARK_MAX",10f),
			Map.entry("MANA_MARK_RADIUS",256f),
			Map.entry("MANA_MARK_HEAL_PERCENT",1f),
			Map.entry("MANA_MARK_DAMAGE_PERCENT",0.005f),
			Map.entry("MANA_MARK_DURATION",8f)
	), args -> {
		float radius = args.player.getFlagF("MANA_MARK_RADIUS");
		float per = args.player.getFlagF("MANA_MARK_HEAL_PERCENT") / 100f;
		float marks = 0;
		Vector2f pos = args.player.getBoundingBox().getMiddle();
		for (Entity entity : Main.serverHandler.getEntities()) {
			if(entity.getBoundingBox().getMiddle().distance(pos) <= radius){
				if(entity.hasFlag("MANA_MARK")){
					marks += entity.getFlagF("MANA_MARK");
					entity.removeFlag("MANA_MARK");
				}
			}
		}
		args.player.addStat(Stat.HEALTH, args.player.getStat(Stat.MAX_HEALTH) * marks * per);
	}, "", "icon_mage_mana_mark")*/;
	private final Map<String, Float> baseFlags;
	private final Consumer<AbilityArgs> trigger;
	private final String argList;
	public final Sprite icon;

	Ability(Map<String, Float> baseFlags, Consumer<AbilityArgs> trigger, String args, String iconKey) {
		this.baseFlags = baseFlags;
		this.trigger = trigger;
		this.argList = args;
		this.icon = new Sprite(iconKey);
	}

	public void trigger(Player player, Object... args){
		trigger.accept(new AbilityArgs(player, args));
	}

	public void addFlags(Player player){
		for (String key : baseFlags.keySet()) {
			if(player.hasFlag(key)){
				player.setFlag(key, player.getFlagF(key) + baseFlags.get(key));
			}
			else {
				player.setFlag(key, baseFlags.get(key));
			}
		}
	}

	public void activateClient(ClientPlayer player){
		Object[] args;
		switch (argList){
			case "mouse" -> {
				Point p = Main.getWindow().getMousePosition();
				if(p == null) return;
				args = new Object[] {player.getX() + p.x - (Main.getWindow().isUndecorated() ? 0 : 8) - Window.WIDTH/2f,
						player.getY() + p.y - (Main.getWindow().isUndecorated() ? 0 : 31) - Window.HEIGHT/2f};
			}
			default -> args = null;
		}
		Main.clientHandler.sendPacket(new ActivateAbilityPacket(this, args));
	}

	public void activateServer(Player player){
		switch (argList){
			case "mouse" -> {
				Point p = Main.getWindow().getMousePosition();
				if(p == null) return;
				trigger(player,(player.getX() + p.x - (Main.getWindow().isUndecorated() ? 0 : 8) - Window.WIDTH/2f),
						(player.getY() + p.y - (Main.getWindow().isUndecorated() ? 0 : 31)) - Window.HEIGHT/2f);
			}
			default -> trigger(player);
		}
	}

	private static class AbilityArgs{
		public final Player player;
		public final Object[] args;

		public AbilityArgs(Player player, Object[] args) {
			this.player = player;
			this.args = args;
		}
	}
}
