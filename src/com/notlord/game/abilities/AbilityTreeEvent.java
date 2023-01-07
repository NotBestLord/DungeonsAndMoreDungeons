package com.notlord.game.abilities;

import com.notlord.game.Entity;
import com.notlord.game.server.Player;
import com.notlord.game.server.Projectile;
import com.notlord.utils.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;

public class AbilityTreeEvent {
	private Consumer<Player> acquireEvent;
	private Function<PlayerEntityInteract, Tuple<Float, Boolean>> takeDamageEvent;
	private Function<PlayerEntityInteract, Tuple<Float, Boolean>> dealProjectileDamageEvent;
	private Function<PlayerEntityInteract, Tuple<Float, Boolean>> dealDamageEvent;
	private Function<PlayerClickEvent, Boolean> meleeAttackEvent;
	private Consumer<PlayerLaunchProjectile> projectileLaunchEvent;
	private Function<Player, Boolean> deathEvent;

	public AbilityTreeEvent() {}

	public AbilityTreeEvent setAcquireEvent(Consumer<Player> acquireEvent) {
		this.acquireEvent = acquireEvent;
		return this;
	}

	public AbilityTreeEvent setTakeDamageEvent(Function<PlayerEntityInteract, Tuple<Float, Boolean>> takeDamageEvent) {
		this.takeDamageEvent = takeDamageEvent;
		return this;
	}

	public AbilityTreeEvent setDealProjectileDamageEvent(Function<PlayerEntityInteract, Tuple<Float, Boolean>> dealProjectileDamageEvent) {
		this.dealProjectileDamageEvent = dealProjectileDamageEvent;
		return this;
	}

	public AbilityTreeEvent setDealDamageEvent(Function<PlayerEntityInteract, Tuple<Float, Boolean>> dealDamageEvent) {
		this.dealDamageEvent = dealDamageEvent;
		return this;
	}

	public AbilityTreeEvent setProjectileLaunchEvent(Consumer<PlayerLaunchProjectile> projectileLaunchEvent) {
		this.projectileLaunchEvent = projectileLaunchEvent;
		return this;
	}

	public AbilityTreeEvent setDeathEvent(Function<Player, Boolean> deathEvent) {
		this.deathEvent = deathEvent;
		return this;
	}

	public AbilityTreeEvent setMeleeAttackEvent(Function<PlayerClickEvent, Boolean> meleeAttackEvent) {
		this.meleeAttackEvent = meleeAttackEvent;
		return this;
	}

	public void equip(Player self){
		if(acquireEvent != null) acquireEvent.accept(self);
	}

	public Tuple<Float, Boolean> dealDamage(Player self, Entity damaged){
		if(dealDamageEvent != null) return dealDamageEvent.apply(new PlayerEntityInteract(self,damaged));
		return new Tuple<>(0f, false);
	}

	public Tuple<Float, Boolean> dealProjectileDamage(Player self, Entity damaged){
		if(dealProjectileDamageEvent != null) return dealProjectileDamageEvent.apply(new PlayerEntityInteract(self,damaged));
		return new Tuple<>(0f, false);
	}

	public Tuple<Float, Boolean> takeDamage(Player self, Entity damager){
		if(takeDamageEvent != null) return takeDamageEvent.apply(new PlayerEntityInteract(self,damager));
		return new Tuple<>(0f, false);
	}

	public boolean performMeleeAttack(Player self, int x, int y){
		if(meleeAttackEvent != null) return meleeAttackEvent.apply(new PlayerClickEvent(self,x,y));
		return false;
	}

	public void performSpecialAttack(Player self, Projectile projectile){
		if(projectileLaunchEvent != null) projectileLaunchEvent.accept(new PlayerLaunchProjectile(self,projectile));
	}

	public boolean deathEvent(Player self) {
		if(deathEvent != null) return deathEvent.apply(self);
		return false;
	}

	public static class PlayerEntityInteract {
		private final Player player;
		private final Entity entity;

		public PlayerEntityInteract(Player player, Entity entity) {
			this.player = player;
			this.entity = entity;
		}

		public Player getPlayer() {
			return player;
		}

		public Entity getEntity() {
			return entity;
		}
	}
	public static class PlayerLaunchProjectile{
		private final Player player;
		private final Projectile projectile;

		public PlayerLaunchProjectile(Player player, Projectile projectile) {
			this.player = player;
			this.projectile = projectile;
		}

		public Player getPlayer() {
			return player;
		}

		public Projectile getProjectile() {
			return projectile;
		}
	}
	public static class PlayerClickEvent{
		private final Player player;
		private final int x,y;

		public PlayerClickEvent(Player player, int x, int y) {
			this.player = player;
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public Player getPlayer() {
			return player;
		}
	}
}
