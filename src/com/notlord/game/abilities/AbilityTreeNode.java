package com.notlord.game.abilities;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.Window;
import com.notlord.game.client.ClientPlayer;
import com.notlord.game.server.Player;
import com.notlord.game.stats.Stat;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.interaction.GuiElement;
import com.notlord.gui.interaction.IGuiClick;
import com.notlord.gui.interaction.PopText;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.AbilityTreeBuyRequest;
import com.notlord.networking.packets.AddToAbilityTreePacket;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityTreeNode extends GuiElement implements IGuiClick {
	private static final Sprite lock = new Sprite("icon_lock");
	public final String key;
	private final List<String> requirements = new ArrayList<>();
	public final AbilityTreeEvent event;
	private boolean owned = false;
	private final NodeSprite sprite;
	private final Sprite hoverPop;
	protected final int xi,yi,s,xl,yl;
	private int ox=0,oy=0;
	private AbilityTree parent;
	private Vector2f popPos = null;

	protected AbilityTreeNode(String key, NodeSprite sprite, int x, int y, int tileSize, int mul, AbilityTreeEvent event, String title, String description) {
		this.key = key;
		this.sprite = sprite;
		xi = x * tileSize * mul;
		yi = y * tileSize * mul;
		xl = xi + sprite.getSprite().getImage().getWidth()/2 - lock.getImage().getWidth()/2;
		yl = yi + sprite.getSprite().getImage().getHeight()/2 - lock.getImage().getHeight()/2;
		s = tileSize * mul;
		this.event = event;
		hoverPop = generatePopup(title, description);
	}

	private AbilityTreeNode(AbilityTreeNode node, AbilityTree parent) {
		this.parent = parent;
		this.key = node.key;
		this.sprite = node.sprite;
		this.xi = node.xi;
		this.yi = node.yi;
		this.xl = node.xl;
		this.yl = node.yl;
		this.ox = node.ox;
		this.oy = node.oy;
		this.s = node.s;
		this.event = node.event;
		this.requirements.addAll(node.requirements);
		this.hoverPop = node.hoverPop;
	}

	public AbilityTreeNode setRequirements(String... requirements){
		this.requirements.addAll(Arrays.asList(requirements));
		return this;
	}

	protected AbilityTreeNode duplicate(AbilityTree parent){
		return new AbilityTreeNode(this, parent);
	}

	@Override
	public void nextTick(float deltaTime) {
		Point p = Main.getWindow().getMousePosition();
		if(p == null || !(yi >= parent.page * Constants.ABILITY_TREE_VIS_HEIGHT && yi < (parent.page+1)*Constants.ABILITY_TREE_VIS_HEIGHT)) return;
		int x = p.x - (Main.getWindow().isUndecorated() ? 0 : 8);
		int y = p.y - (Main.getWindow().isUndecorated() ? 0 : 31);
		if(new AABB(ox+xi,oy+yi - (parent.page * Constants.ABILITY_TREE_VIS_HEIGHT),
				ox+xi+s,oy+yi+s - (parent.page * Constants.ABILITY_TREE_VIS_HEIGHT)).contains(x,y)){
			popPos = new Vector2f(x,y);
			if(x + hoverPop.getImage().getWidth() + 16 > Window.WIDTH){
				popPos.x = Window.WIDTH - hoverPop.getImage().getWidth()-16;
			}
			if(y + hoverPop.getImage().getHeight() > Window.HEIGHT){
				popPos.y = Window.HEIGHT - hoverPop.getImage().getHeight()-16;
			}
		}
		else if(popPos != null){
			popPos = null;
		}
	}

	public void setOffset(int x, int y){
		ox=x;
		oy=y;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		if (owned && yi >= offsetY && yi < (parent.page+1)*Constants.ABILITY_TREE_VIS_HEIGHT) {
			sprite.getSprite().draw(g, ox + xi, oy + yi - offsetY, ox + xi + s, oy + yi - offsetY + s);
		} else if (!owned && yi >= offsetY && yi < (parent.page+1)*Constants.ABILITY_TREE_VIS_HEIGHT && !parent.meetsRequirements(requirements)) {
			lock.draw(g, ox + xl, oy + yl - offsetY);
		}
	}

	public void renderHover(Graphics g, int offsetY){
		if(yi >= offsetY && popPos != null){
			hoverPop.draw(g, popPos.getIntX(), popPos.getIntY());
		}
	}

	public void buyAttempt(int x, int y){
		if(!parent.meetsRequirements(requirements)){
			PopText txt = new PopText(new Font(Constants.DEFAULT_FONT.getName(),Font.BOLD, 18),Color.red,"You Do Not Meet Requirements",
					x - (Window.WIDTH / 2f) +parent.owner.getX(),y - (Window.HEIGHT / 2f) + parent.owner.getY(), 1,0.5f,true,"");
			Main.getPanel().addObject(txt);
			return;
		}
		//
		if(parent.owner.getStatHandler().getStat(Stat.AP) < NodeSprite.price(sprite)){
			PopText txt = new PopText(new Font(Constants.DEFAULT_FONT.getName(),Font.BOLD, 18),Color.red,"You Need "+NodeSprite.price(sprite)+" Ability Points",
					x - (Window.WIDTH / 2f) +parent.owner.getX(),y - (Window.HEIGHT / 2f) + parent.owner.getY(), 1,0.5f,true,"");
			Main.getPanel().addObject(txt);
			return;
		}
		//
		if(parent.owner instanceof Player p){
			setOwned();
			event.equip(p);
			p.addStat(Stat.AP, -NodeSprite.price(sprite));
			if(p.getClientInstance() != null){
				p.getClientInstance().send(new AddToAbilityTreePacket(key));
			}
		}
		else if(parent.owner instanceof ClientPlayer){
			Main.clientHandler.sendPacket(new AbilityTreeBuyRequest(key,x,y));
		}
	}

	public void setOwned() {
		this.owned = true;
		parent.ownedNodes.add(this.key);
	}

	@Override
	public boolean mouseClick(int x, int y, int button) {
		if(button == 1 && new AABB(ox+xi,oy+yi - (parent.page * Constants.ABILITY_TREE_VIS_HEIGHT),
				ox+xi+s,oy+yi+s - (parent.page * Constants.ABILITY_TREE_VIS_HEIGHT)).contains(x,y) &&
				yi >= parent.page*Constants.ABILITY_TREE_VIS_HEIGHT && yi < (parent.page+1)*Constants.ABILITY_TREE_VIS_HEIGHT){
			// handle click
			if(!owned){
				buyAttempt(x,y);
			}
			return true;
		}
		return false;
	}

	@Override
	public void mouseRelease(int x, int y, int button) {

	}

	public boolean isOwned() {
		return owned;
	}

	public static Sprite generatePopup(String title, String description){
		Canvas c = Constants.DEBUG_CANVAS;
		Font titleFont = new Font("rainyhearts", Font.BOLD, 18);
		Font descFont = new Font("rainyhearts", Font.PLAIN, 16);
		double hi = 16 + c.getFontMetrics(titleFont).getStringBounds(title, null).getHeight() + description.split("\n").length * 4;
		double wi = c.getFontMetrics(titleFont).getStringBounds(title, null).getWidth();
		for (String s : description.split("\n")) {
			hi += c.getFontMetrics(descFont).getStringBounds(s, null).getHeight();
			wi = Math.max(wi,c.getFontMetrics(descFont).getStringBounds(s, null).getWidth());
		}
		int w = (int) wi + 16;
		int h = (int) hi;
		BufferedImage image = new BufferedImage(w, h,BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.decode("#060019"));
		g.fillRect(0,0,w,h);
		g.setColor(Color.decode("#140071"));
		g.fillRect(4,4,w-8,h-8);
		g.setColor(Color.decode("#0C004C"));
		g.fillRect(8,8,w-16,h-16);
		g.setFont(titleFont);
		int dh = 8;
		String[] titleArgs = title.split(":");
		if(titleArgs.length == 2){
			g.setColor(Color.decode(titleArgs[0]));
			g.drawString(titleArgs[1],10, (int) (8 + g.getFontMetrics(titleFont).getStringBounds(titleArgs[1], g).getHeight()));
			dh += g.getFontMetrics(titleFont).getStringBounds(titleArgs[1], g).getHeight();
		}
		else{
			g.setColor(Color.white);
			g.drawString(title,10, (int) (8 + g.getFontMetrics(titleFont).getStringBounds(title, g).getHeight()));
			dh += g.getFontMetrics(titleFont).getStringBounds(title, g).getHeight();
		}
		dh += 4;
		g.setFont(descFont);
		for (String s : description.split("\n")) {
			String[] sa = s.split(":");
			if(sa.length == 2){
				g.setColor(Color.decode(sa[0]));
				g.drawString(sa[1],10, (int) (dh + g.getFontMetrics(descFont).getStringBounds(sa[1], g).getHeight()));
				dh += g.getFontMetrics(descFont).getStringBounds(sa[1], g).getHeight();
			}
			else{
				g.setColor(Color.white);
				g.drawString(s,10, (int) (dh + g.getFontMetrics(descFont).getStringBounds(s, g).getHeight()));
				dh += g.getFontMetrics(descFont).getStringBounds(s, g).getHeight();
			}
			dh += 4;
		}
		return new Sprite(image);
	}

	protected enum NodeSprite{
		MINOR("minor"),MAJOR("major"),ABILITY_KNIGHT("knight_ability"),ABILITY_MAGE("mage_ability"),ABILITY_RANGER("ranger_ability");
		private final String key;
		NodeSprite(String key) {
			this.key = key;
		}
		public Sprite getSprite(){
			return new Sprite("ability_tree_"+key+"_owned");
		}
		public static int price(NodeSprite n){
			return switch (n){
				case MINOR -> 1;
				case MAJOR -> 2;
				default -> 3;
			};
		}

	}

}
