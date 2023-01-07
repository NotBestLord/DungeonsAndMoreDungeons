package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.Window;
import com.notlord.gui.images.Sprite;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HoverImage extends GuiElement {
	private final Sprite sprite;
	private final BufferedImage textImage;
	private final BufferedImage hoverImage;
	private final Vector2f hoverPos;
	private final AABB aabb;
	private boolean isHovered = false;
	/**
	 * @param anchor "center"=x,y is offset from middle of screen,""=x,y is (x,y)
	 */
	public HoverImage(Sprite sprite, int x, int y, int width, int height, String anchor, BufferedImage hoverImage, Vector2f hoverPos) {
		this.sprite = sprite;
		this.hoverImage = hoverImage;
		this.hoverPos = hoverPos;
		this.textImage = null;
		int xa = anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0;
		int ya = anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0;
		aabb = new AABB(
				x + xa,
				y + ya,
				x + xa + width,
				y + ya + height);
	}

	public HoverImage(String text, int x, int y, int padding, int fontSize, boolean centered, String anchor, BufferedImage hoverImage, Vector2f hoverPos) {
		this.hoverImage = hoverImage;
		this.hoverPos = hoverPos;
		sprite = null;
		int xa = anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0;
		int ya = anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0;
		Font font = new Font(Constants.DEFAULT_FONT.getName(), Constants.DEFAULT_FONT.getStyle(),fontSize);
		int width = (int) (padding*2 +
				Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(text,Constants.DEBUG_CANVAS.getGraphics()).getWidth());
		int height = (int) (padding*2 +
				Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(text,Constants.DEBUG_CANVAS.getGraphics()).getHeight());
		if(centered){
			xa -= width / 2;
			ya -= height / 2;
		}
		this.textImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = this.textImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0,0,width, height);
		g.setColor(Color.white);
		g.setFont(font);
		g.drawString(text, padding, height - padding*2);
		aabb = new AABB(
				x + xa,
				y + ya,
				x + xa + width,
				y + ya + height);
	}

	@Override
	public void render(Graphics g,int offsetX, int offsetY) {
		if (sprite != null)
			sprite.draw(g, aabb.min.getIntX(), aabb.min.getIntY(), aabb.max.getIntX(), aabb.max.getIntY());
		else if (textImage != null) {
			g.drawImage(textImage, aabb.min.getIntX(), aabb.min.getIntY(), null);
		}
		if(isHovered){
			g.drawImage(hoverImage, hoverPos.getIntX(), hoverPos.getIntY(), null);
		}
	}

	@Override
	public void nextTick(float deltaTime) {
		Point pos;
		if((pos = Main.getWindow().getMousePosition()) != null){
			if(aabb.contains(pos.x-8,pos.y-31)){
				if(!isHovered) isHovered = true;
			}
			else{
				if(isHovered) isHovered = false;
			}
		}
	}
}
