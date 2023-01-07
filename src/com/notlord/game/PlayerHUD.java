package com.notlord.game;

import com.notlord.Main;
import com.notlord.Window;
import com.notlord.game.abilities.Ability;
import com.notlord.game.client.ClientPlayer;
import com.notlord.game.server.Player;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.interaction.IGuiClick;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;

import java.awt.*;
import java.awt.image.BufferedImage;


public class PlayerHUD implements IRender, IGuiClick {
	private final Sprite bottomLayer = new Sprite("gui_hud_0");
	private final Sprite expBar = new Sprite("gui_hud_1");
	private final Sprite healthBar = new Sprite("gui_hud_2");
	private final Sprite chargeBar = new Sprite("gui_hud_3");
	private final Sprite abilityRack = new Sprite("gui_ability_rack");
	private final Sprite abilityRackNum = new Sprite("gui_ability_rack_numbers");
	private final Entity owner;
	private String lvl = "1";

	public PlayerHUD(Entity owner) {
		this.owner = owner;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		if(owner != null){
			int w;
			g.drawImage(bottomLayer.getImage(),0, Window.HEIGHT-bottomLayer.getImage().getHeight(),null);
			if(owner.getStatHandler().getStat(Stat.MAX_HEALTH) != 0) {
				w = (int) (Math.min(owner.getStatHandler().getStat(Stat.HEALTH) / owner.getStatHandler().getStat(Stat.MAX_HEALTH), 1f) * 128);
				g.drawImage(healthBar.getImage().getSubimage(0,0,64+w,healthBar.getImage().getHeight()),
						0, Window.HEIGHT - healthBar.getImage().getHeight(), null);
			}
			if(owner.getStatHandler().getStat(Stat.MAX_EXP) != 0) {
				w = (int) ((owner.getStatHandler().getStat(Stat.EXP) / owner.getStatHandler().getStat(Stat.MAX_EXP)) * 52f);
				BufferedImage image = expBar.getImage().getSubimage(0, expBar.getImage().getHeight() - w - 4, expBar.getImage().getWidth(), w+4);
				g.drawImage(image, 0, Window.HEIGHT - image.getHeight(), null);
			}
			if(owner.getStatHandler().getStat(Stat.MAX_MANA) != 0) {
				w = (int) ((owner.getStatHandler().getStat(Stat.MANA) / owner.getStatHandler().getStat(Stat.MAX_MANA)) * 128);
				g.drawImage(chargeBar.getImage().getSubimage(0,0,64+w,chargeBar.getImage().getHeight()),
						0, Window.HEIGHT - chargeBar.getImage().getHeight(), null);
			}
			if(!lvl.equals(""+((int) owner.getStatHandler().getStat(Stat.LEVEL)))) lvl = ""+((int) owner.getStatHandler().getStat(Stat.LEVEL));
			g.setColor(Color.white);
			g.setFont(new Font("Minecraft", Font.BOLD, 24));
			g.drawString(lvl, 24 - ((int) g.getFontMetrics().getStringBounds(lvl,null).getWidth()/2),
					690 + ((int) g.getFontMetrics().getStringBounds(lvl,null).getHeight()/2));

			//
			if(owner instanceof Player player && !player.abilities.isEmpty()) {
				g.drawImage(abilityRack.getImage(), Window.WIDTH - abilityRack.getImage().getWidth(), Window.HEIGHT - abilityRack.getImage().getHeight(),null);
				int x = Window.WIDTH - abilityRack.getImage().getWidth() + 8;
				int y = Window.HEIGHT - abilityRack.getImage().getHeight() + 8;
				for (int i = 0; i < player.abilities.size(); i++) {
					player.abilities.get(i).icon.draw(g,x,y);
					x += player.abilities.get(i).icon.getImage().getWidth() + 8;
				}
				abilityRackNum.draw(g, Window.WIDTH - abilityRackNum.getImage().getWidth(), Window.HEIGHT - abilityRackNum.getImage().getHeight());
			}
			else if(owner instanceof ClientPlayer player && !player.abilities.isEmpty()) {
				g.drawImage(abilityRack.getImage(), Window.WIDTH - abilityRack.getImage().getWidth(), Window.HEIGHT - abilityRack.getImage().getHeight(),null);
				int x = Window.WIDTH - abilityRack.getImage().getWidth() + 8;
				int y = Window.HEIGHT - abilityRack.getImage().getHeight() + 8;
				for (int i = 0; i < player.abilities.size(); i++) {
					player.abilities.get(i).icon.draw(g,x,y);
					x += player.abilities.get(i).icon.getImage().getWidth() + 8;
				}
				abilityRackNum.draw(g, Window.WIDTH - abilityRackNum.getImage().getWidth(), Window.HEIGHT - abilityRackNum.getImage().getHeight());
			}
			Point pos;
			if(dragNDrop != -1 && (pos = Main.getWindow().getMousePosition()) != null){
				if(owner instanceof Player player && player.abilities.size() > dragNDrop) {
					player.abilities.get(dragNDrop).icon.draw(g,pos.x-8,pos.y-31);
				}
				else if(owner instanceof ClientPlayer player && player.abilities.size() > dragNDrop) {
					player.abilities.get(dragNDrop).icon.draw(g,pos.x-8,pos.y-31);
				}
			}
		}
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.GUI;
	}

	private int dragNDrop = -1;
	@Override
	public boolean mouseClick(int x, int y, int button) {
		if(button == 1 && owner instanceof Player player && !player.abilities.isEmpty()){
			int dx = Window.WIDTH - abilityRack.getImage().getWidth() + 8;
			int dy = Window.HEIGHT - abilityRack.getImage().getHeight() + 8;
			for (int i = 0; i < player.abilities.size(); i++) {
				AABB aabb = new AABB(dx,dy,dx+48,dy+48);
				if(aabb.contains(x,y)){
					dragNDrop = i;
					return true;
				}
				dx += player.abilities.get(i).icon.getImage().getWidth() + 8;
			}
		}
		else if(button == 1 && owner instanceof ClientPlayer player && !player.abilities.isEmpty()){
			int dx = Window.WIDTH - abilityRack.getImage().getWidth() + 8;
			int dy = Window.HEIGHT - abilityRack.getImage().getHeight() + 8;
			for (int i = 0; i < player.abilities.size(); i++) {
				AABB aabb = new AABB(dx,dy,dx+48,dy+48);
				if(aabb.contains(x,y)){
					dragNDrop = i;
					return true;
				}
				dx += player.abilities.get(i).icon.getImage().getWidth() + 8;
			}
		}
		return false;
	}

	@Override
	public void mouseRelease(int x, int y, int button) {
		int d = dragNDrop;
		dragNDrop = -1;
		if(owner instanceof Player player && !player.abilities.isEmpty()){
			int dx = Window.WIDTH - abilityRack.getImage().getWidth() + 8;
			int dy = Window.HEIGHT - abilityRack.getImage().getHeight() + 8;
			for (int i = 0; i < player.abilities.size(); i++) {
				AABB aabb = new AABB(dx,dy,dx+48,dy+48);
				if(aabb.contains(x,y) && d != -1){
					Ability ability = player.abilities.get(d);
					player.abilities.set(d, player.abilities.get(i));
					player.abilities.set(i, ability);
					break;
				}
				dx += player.abilities.get(i).icon.getImage().getWidth() + 8;
			}
		}
		else if(owner instanceof ClientPlayer player && !player.abilities.isEmpty()){
			int dx = Window.WIDTH - abilityRack.getImage().getWidth() + 8;
			int dy = Window.HEIGHT - abilityRack.getImage().getHeight() + 8;
			for (int i = 0; i < player.abilities.size(); i++) {
				AABB aabb = new AABB(dx,dy,dx+48,dy+48);
				if(aabb.contains(x,y) && d != -1){
					Ability ability = player.abilities.get(d);
					player.abilities.set(d, player.abilities.get(i));
					player.abilities.set(i, ability);
					break;
				}
				dx += player.abilities.get(i).icon.getImage().getWidth() + 8;
			}
		}
	}
}
