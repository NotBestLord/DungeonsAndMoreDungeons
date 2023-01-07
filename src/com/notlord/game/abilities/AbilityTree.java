package com.notlord.game.abilities;


import com.notlord.Main;
import com.notlord.Window;
import com.notlord.game.Entity;
import com.notlord.game.server.DamageSource;
import com.notlord.game.server.Projectile;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.interaction.GuiElement;
import com.notlord.gui.interaction.IGuiClick;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.ProjectilePacket;
import com.notlord.utils.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.notlord.Constants.*;

public class AbilityTree extends GuiElement implements IGuiClick {
	private static final Map<String, AbilityTree> BASE_TREES = new HashMap<>();
	static {
		// initiate trees
		AbilityTree mage = new AbilityTree("mage");
		BASE_TREES.put("mage", mage);
		mage.addNode(new AbilityTreeNode("mana_blast", AbilityTreeNode.NodeSprite.ABILITY_MAGE, 5,1,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addAbility(Ability.MAGE_MANA_BLAST)),
				"Mana Blast", """
				Activate:
				Shoot A Large Ball Of
				Condensed Mana, Which
				Damages Enemies.
				3 AP
				"""));

		mage.addNode(new AbilityTreeNode("cheaper_mana_blast", AbilityTreeNode.NodeSprite.MINOR, 5,3,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.setFlag("MANA_BLAST_COST", player.getFlagF("MANA_BLAST_COST") - 5f)),
				"Cheaper Mana Blast","Mana Blast Cost -5\n1 AP").setRequirements("mana_blast"));

		mage.addNode(new AbilityTreeNode("blink", AbilityTreeNode.NodeSprite.ABILITY_MAGE, 5,5,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addAbility(Ability.MAGE_BLINK)),
				"Blink", """
				Activate:
				Become Intangible And Teleport A
				Short Distance In Your Current
				Direction Of Movement.
				3 AP
				""").setRequirements("cheaper_mana_blast"));

		mage.addNode(new AbilityTreeNode("farther_blink", AbilityTreeNode.NodeSprite.MINOR, 5,7,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.setFlag("BLINK_DISTANCE", player.getFlagF("BLINK_DISTANCE") + 64f)),
				"Farther Blink","Blink will teleport you farther\n1 AP").setRequirements("blink"));

		mage.addNode(new AbilityTreeNode("main_ranged", AbilityTreeNode.NodeSprite.MAJOR, 4,8,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> {
					player.setFlag("MAIN_RANGED_SIZE", 8f);
					player.setFlag("MAIN_RANGED_DURATION", 5f);
					player.setFlag("MAIN_RANGED_SPEED", 128f);
					player.setFlag("MAIN_RANGED_DAMAGE_PERCENT", 0.75f);
					player.setFlag("MAIN_RANGED_COST", 5f);
				}).setMeleeAttackEvent(e -> {
					float s = e.getPlayer().getFlagF("MAIN_RANGED_SIZE");
					float spd = e.getPlayer().getFlagF("MAIN_RANGED_SPEED");
					float dmg = e.getPlayer().getFlagF("MAIN_RANGED_DAMAGE_PERCENT") * Stat.calculateAttack(e.getPlayer().getStat(Stat.ATTACK));
					float dur = e.getPlayer().getFlagF("MAIN_RANGED_DURATION");
					float cost = e.getPlayer().getFlagF("MAIN_RANGED_COST");
					if(e.getPlayer().getStat(Stat.MANA) < cost) return true;
					e.getPlayer().addStat(Stat.MANA, -cost);
					float sx=e.getPlayer().getBoundingBox().getMiddle().x,sy=e.getPlayer().getBoundingBox().min.y;
					float angle = (float) Math.toDegrees(Math.atan2(e.getY() - sy, e.getX() - sx));
					if(angle < 0){
						angle += 360;
					}
					if(angle <= 270 && angle >= 90 && !e.getPlayer().isFlipped()){
						e.getPlayer().flip();
					}
					if((angle <= 90 || angle >= 270) && e.getPlayer().isFlipped()){
						e.getPlayer().flip();
					}
					Projectile projectile = new Projectile("particle_magic_0", DamageSource.PLAYER, e.getPlayer(),
							new AABB(sx-s,sy-s,sx+s,sy+s),dur,angle,spd,dmg);
					Main.serverHandler.server.sendAll(new ProjectilePacket(projectile.getId(),
							"particle_magic_0", dur, s, angle, new Vector2f(sx, sy), projectile.getVelocity()));
					return true;
				}),
				"Expelliarmus", """
				Main Attack Shoots A Small Mana Star.
				(Instead Of Attacking)
				The Attack Will Cost A Small Amount
				Of Mana.
				2 AP
				""").setRequirements("farther_blink"));

		mage.addNode(new AbilityTreeNode("mana_pocket", AbilityTreeNode.NodeSprite.MAJOR, 6,8,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addStat(Stat.MAX_MANA, 20f)),
				"Mana Pocket","Increase Max Mana\n2 AP").setRequirements("farther_blink"));

		mage.addNode(new AbilityTreeNode("stronger_mana_blast", AbilityTreeNode.NodeSprite.MINOR, 3,9,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.setFlag("MANA_BLAST_DAMAGE", player.getFlagF("MANA_BLAST_DAMAGE") + 10f)),
				"Stronger Mana Blast","Increase Mana Blast Damage\n1 AP").setRequirements("main_ranged&OR&mana_pocket"));

		mage.addNode(new AbilityTreeNode("longer_mana_blast", AbilityTreeNode.NodeSprite.MINOR, 7,9,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> {
					player.setFlag("MANA_BLAST_DURATION", player.getFlagF("MANA_BLAST_DURATION") + 2.5f);
					player.setFlag("MANA_BLAST_SPEED", player.getFlagF("MANA_BLAST_SPEED") + 64f);
				}),
				"Longer Mana Blast","Increase Mana Blast Speed & Duration\n1 AP").setRequirements("main_ranged&OR&mana_pocket"));

		mage.addNode(new AbilityTreeNode("heal", AbilityTreeNode.NodeSprite.ABILITY_MAGE, 5,10,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addAbility(Ability.MAGE_HEAL)),
				"Heal", """
				Activate:
				Heal Self For 5% Of
				Max HP Per Second
				For 3 Seconds, And Nearby
				Players For 2.5% Max HP
				3 AP""").setRequirements("stronger_mana_blast&OR&longer_mana_blast"));

		mage.addNode(new AbilityTreeNode("increase_survivability", AbilityTreeNode.NodeSprite.MINOR, 2,11,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> {
					player.addStat(Stat.SPEED, 4);
					player.addStat(Stat.MAX_HEALTH, 25);
					player.addStat(Stat.HEALTH, 25);
					player.addStat(Stat.DEFENSE, 5);
				}),
				"Increased Survivability", """
				Increase Base Speed, Defense And Health.
				1 AP
				""").setRequirements("stronger_mana_blast&OR&heal"));

		mage.addNode(new AbilityTreeNode("heal_aura", AbilityTreeNode.NodeSprite.MAJOR, 2,13,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.setFlag("HEAL_DAMAGE_PERCENT", player.getFlagF("HEAL_DAMAGE_PERCENT") + 0.25f)),
				"Heal Aura", """
				Enemies In 'Heal' Area Take Damage.
				(Requires 'Heal')
				2 AP
				""").setRequirements("heal&&increase_survivability"));

		mage.addNode(new AbilityTreeNode("mana_cache", AbilityTreeNode.NodeSprite.ABILITY_MAGE, 2,15,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addAbility(Ability.MAGE_MANA_CACHE)).setDealDamageEvent(
						e -> {
							if(e.getPlayer().hasFlag("MANA_CACHE_ADD")){
								float max = e.getPlayer().getFlagF("MANA_CACHE_MAX");
								float mana = e.getPlayer().getFlagF("MANA_CACHE_MANA");
								float add = e.getPlayer().getFlagF("MANA_CACHE_ADD");
								if(mana + add > max){
									mana = max;
								}
								else mana += add;
								e.getPlayer().setFlag("MANA_CACHE_MANA", mana);
							}
							return new Tuple<>(0f,false);
						}
				).setDealProjectileDamageEvent(
						e -> {
							if(e.getPlayer().hasFlag("MANA_CACHE_ADD") && e.getPlayer().getFlagF("MANA_CACHE_PROJ") != 0){
								float max = e.getPlayer().getFlagF("MANA_CACHE_MAX");
								float mana = e.getPlayer().getFlagF("MANA_CACHE_MANA");
								float add = e.getPlayer().getFlagF("MANA_CACHE_ADD");
								if(mana + add > max){
									mana = max;
								}
								else mana += add;
								e.getPlayer().setFlag("MANA_CACHE_MANA", mana);
							}
							return new Tuple<>(0f,false);
						}
				),
				"Mana Cache", """
				Main Attack Increases The Mana
				Stored In The 'Mana Cache' When
				An Enemy Is Hit.
				(Max 40)
				
				Activate:
				The Mana From The Cache Will Be
				Deposited Into The Player's Usable Mana.
				(Will Not Overflow)
				3 AP""").setRequirements("heal_aura"));

		mage.addNode(new AbilityTreeNode("larger_heal", AbilityTreeNode.NodeSprite.MINOR, 5,12,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> {
					player.setFlag("HEAL_RADIUS", player.getFlagF("HEAL_RADIUS") + 32f);
				}),
				"Larger Heal","Increase 'Heal' Range\n1 AP").setRequirements("heal"));

		mage.addNode(new AbilityTreeNode("life_steal", AbilityTreeNode.NodeSprite.MAJOR, 6,13,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.setFlag("LIFE_STEAL", 1f)).setDealDamageEvent(e -> {
					if(e.getPlayer().hasFlag("LIFE_STEAL")) {
						float hp = e.getPlayer().getStat(Stat.HEALTH);
						float max = e.getPlayer().getStat(Stat.MAX_HEALTH);
						float add = max * e.getPlayer().getFlagF("LIFE_STEAL") / 100f;
						if(hp + add > max) e.getPlayer().setStat(Stat.HEALTH, max);
						else e.getPlayer().addStat(Stat.HEALTH, add);
					}
					return new Tuple<>(0f, false);
				}), "Life Steal", """
				Main Attack Heals Self When
				An Enemy Is Hit.
				2 AP
				""").setRequirements("larger_heal"));

		mage.addNode(new AbilityTreeNode("vitality_0", AbilityTreeNode.NodeSprite.MINOR, 7,11,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addStat(Stat.HEALTH_REGEN, 0.25f)),
				"Vitality I", """
				Recover Health Over Time.
				1 AP""").setRequirements("heal&OR&longer_mana_blast"));

		mage.addNode(new AbilityTreeNode("healthy_0", AbilityTreeNode.NodeSprite.MINOR, 9,11,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> {
					player.addStat(Stat.MAX_HEALTH, 25f);
					player.addStat(Stat.HEALTH, 25f);
				}),
				"Healthy I", """
				Increase Max Health.
				1 AP""").setRequirements("heal&OR&longer_mana_blast"));

		mage.addNode(new AbilityTreeNode("blink_damage", AbilityTreeNode.NodeSprite.MAJOR, 8,13,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.setFlag("BLINK_DAMAGE", 10f)),
				"Blink Damage Aura", """
				'Blink' Will Damage Enemies
				Around End Location.
				2 AP""").setRequirements("vitality_0&OR&healthy_0"));

		mage.addNode(new AbilityTreeNode("fire_totem", AbilityTreeNode.NodeSprite.ABILITY_MAGE, 8,15,ABILITY_TREE_TILE_SIZE,ABILITY_TREE_SIZE_MUL,
				new AbilityTreeEvent().setAcquireEvent(player -> player.addAbility(Ability.MAGE_FIRE_TOTEM)),
				"Fire Totem", """
				Activate:
				Spawn A Totem That Periodically
				Shoots A Projectile At  A Nearby Enemy.
				The Totem Will Disappear After A Certain
				Period Of Time Has Passed.
				3 AP""").setRequirements("blink_damage"));
		/*
		 * last ability tree purchase (10/20 AP & lvl 100) => Reset Ability Tree + Ability Combination + 1 Characteristic
		 *
		 * Characteristic = permanent passive skill, ex:
		 *      - Blood Mage (Mage) - All Mana is replaced by hp.
		 *      - Enchanter (Mage) - Buff nearby players.
		 *      - K.O (Knight) - Chance to insta-kill low hp enemies.
		 *      - AK-47 (Ranger) - Increase Main Attack damage, fire speed, and magazine size.
		 *      - Teleports Behind You (Knight) - When ability '...' is used, teleport behind nearby enemy.
		 *      - Lucky 13 (...) - 13th hit does 4x damage.
		 */
	}

	static{
		AbilityTree knight = new AbilityTree("knight");
		BASE_TREES.put("knight", knight);

	}

	static{
		AbilityTree ranger = new AbilityTree("ranger");
		BASE_TREES.put("ranger", ranger);

	}

	protected static AbilityTree getNew(String key, Entity owner){
		return BASE_TREES.get(key).duplicate(owner);
	}

	private final Sprite ARROW_UP = new Sprite("gui_arrow_up"),ARROW_DOWN = new Sprite("gui_arrow_down"),CLOSE = new Sprite("gui_close");

	private final List<AbilityTreeNode> nodes = new ArrayList<>();
	protected final List<String> ownedNodes = new ArrayList<>();
	private final Sprite sprite;
	private final int x,y;
	protected int page = 0;
	private final int lastPage;
	protected Entity owner;

	public AbilityTree(String key) {
		sprite = new Sprite("ability_tree_"+key);
		x = Window.WIDTH - (sprite.getImage().getWidth()*ABILITY_TREE_SIZE_MUL);
		y = (Window.HEIGHT - (ABILITY_TREE_VIS_HEIGHT*ABILITY_TREE_SIZE_MUL)) / 2;
		lastPage = (sprite.getImage().getHeight() / ABILITY_TREE_VIS_HEIGHT) - 1;
	}

	private AbilityTree(AbilityTree other, Entity owner) {
		for (AbilityTreeNode node : other.nodes) {
			nodes.add(node.duplicate(this));
		}
		this.owner = owner;
		this.sprite = other.sprite;
		this.x = other.x;
		this.y = other.y;
		this.lastPage = other.lastPage;
	}

	public void addNode(AbilityTreeNode node){
		nodes.add(node);
		node.setOffset(x,y);
	}

	private AbilityTree duplicate(Entity owner){
		return new AbilityTree(this, owner);
	}

	public List<AbilityTreeNode> getNodes() {
		return new ArrayList<>(nodes);
	}

	public AbilityTreeNode getNode(String key){
		for (AbilityTreeNode n : nodes) {
			if(n.key.equals(key)) return n;
		}
		return null;
	}

	public boolean meetsRequirements(List<String> requirements){
		for (String s : requirements) {
			if(s.contains("&OR&")){
				String[] args = s.split("&OR&");
				boolean b = false;
				for (String a : args) {
					if (ownedNodes.contains(a)) {
						b = true;
						break;
					}
				}
				if(!b) return false;
			}
			else {
				String[] args = s.split("&&");
				for (String a : args) {
					if(!ownedNodes.contains(a)) return false;
				}
			}
		}
		return true;
	}
	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		// render tree
		g.drawImage(sprite.getImage(),x,y,x+sprite.getImage().getWidth()*ABILITY_TREE_SIZE_MUL,y+ABILITY_TREE_SIZE_MUL*ABILITY_TREE_VIS_HEIGHT,
					0,page*ABILITY_TREE_VIS_HEIGHT,sprite.getImage().getWidth(),(page+1)*ABILITY_TREE_VIS_HEIGHT,null);
		g.drawImage(CLOSE.getImage(),x,y,CLOSE.getImage().getWidth()*ABILITY_TREE_SIZE_MUL,CLOSE.getImage().getHeight()*ABILITY_TREE_SIZE_MUL,null);
		//
		g.setColor(Color.white);
		g.setFont(new Font("Minecraft", Font.PLAIN,12));
		String s = "Points: " + owner.getStatHandler().getStat(Stat.AP);
		g.drawString(s,(int) (x+(sprite.getImage().getWidth()*ABILITY_TREE_SIZE_MUL - g.getFontMetrics().getStringBounds(s,null).getWidth())/2),
				(int) (y+ g.getFontMetrics().getStringBounds(s,null).getHeight()));
		//
		if(page != 0){
			g.drawImage(ARROW_UP.getImage(),
					Window.WIDTH - (ARROW_UP.getImage().getWidth()*ABILITY_TREE_SIZE_MUL),y,
					ARROW_UP.getImage().getWidth()*ABILITY_TREE_SIZE_MUL,
					ARROW_UP.getImage().getHeight()*ABILITY_TREE_SIZE_MUL,null);
		}
		if(page != lastPage){
			g.drawImage(ARROW_DOWN.getImage(),
					Window.WIDTH - (ARROW_DOWN.getImage().getWidth()*ABILITY_TREE_SIZE_MUL),
					y+((ABILITY_TREE_VIS_HEIGHT-ARROW_DOWN.getImage().getHeight())*ABILITY_TREE_SIZE_MUL),
					ARROW_DOWN.getImage().getWidth()*ABILITY_TREE_SIZE_MUL,
					ARROW_DOWN.getImage().getHeight()*ABILITY_TREE_SIZE_MUL,null);
		}
		// render nodes
		for (AbilityTreeNode n : nodes) {
			n.render(g,0,page*ABILITY_TREE_VIS_HEIGHT);
		}
		for (AbilityTreeNode n : nodes) {
			n.renderHover(g,page*ABILITY_TREE_VIS_HEIGHT);
		}
	}

	@Override
	public boolean mouseClick(int mx, int my, int button) {
		boolean click = false;
		for (AbilityTreeNode n : nodes) {
			if(n.yi >= page*ABILITY_TREE_VIS_HEIGHT && n.yi <= (page+1)*ABILITY_TREE_VIS_HEIGHT) click = click || n.mouseClick(mx,my,button);
		}

		if(new AABB(x,y,x+(CLOSE.getImage().getWidth()*ABILITY_TREE_SIZE_MUL),y+(CLOSE.getImage().getHeight()*ABILITY_TREE_SIZE_MUL)).contains(mx,my)){
			click = true;
			owner.setFlag("ability_tree_open", 0);
			Main.getPanel().removeObject(this);
		}

		if(page != 0 && new AABB(Window.WIDTH - (ARROW_UP.getImage().getWidth()*ABILITY_TREE_SIZE_MUL),y,
				Window.WIDTH,y+(ARROW_UP.getImage().getHeight()*ABILITY_TREE_SIZE_MUL)).contains(mx,my)){
			click = true;
			page--;
		}

		if(page != lastPage && new AABB(Window.WIDTH - (ARROW_DOWN.getImage().getWidth()*ABILITY_TREE_SIZE_MUL),
				y+((ABILITY_TREE_VIS_HEIGHT-ARROW_DOWN.getImage().getHeight())*ABILITY_TREE_SIZE_MUL), Window.WIDTH,
				y+(ABILITY_TREE_VIS_HEIGHT*ABILITY_TREE_SIZE_MUL)).contains(mx,my)){
			click = true;
			page++;
		}
		return click;
	}

	@Override
	public void mouseRelease(int x, int y, int button) {

	}

	@Override
	public void nextTick(float deltaTime) {
		nodes.forEach(node -> {
			if(node.yi >= page*ABILITY_TREE_VIS_HEIGHT)
				node.nextTick(deltaTime);
		});
	}

	public enum AbilityTreeType{
		KNIGHT("knight", "player_blue"), MAGE("mage", "mage_blue"),RANGER("ranger", "player_blue");
		private final String abilityTreeKey;
		private final String assetKey;

		AbilityTreeType(String abilityTreeKey, String assetKey) {
			this.abilityTreeKey = abilityTreeKey;
			this.assetKey = assetKey;
		}

		public AbilityTree getNewAbilityTree(Entity owner) {
			return AbilityTree.getNew(abilityTreeKey, owner);
		}

		public String getAssetKey() {
			return assetKey;
		}
	}
}
