package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.game.IEvent;
import com.notlord.gui.images.Sprite;
import com.notlord.math.AABB;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Button extends GuiElement implements IGuiClick {
	private final Sprite sprite;
	private final BufferedImage textImage;
	private IEvent clickEvent;
	private final AABB aabb;
	/**
	 * @param anchor "center"=x,y is offset from middle of screen,""=x,y is (x,y)
	 */
	public Button(Sprite sprite, int x, int y, int width, int height, String anchor) {
		this.sprite = sprite;
		this.textImage = null;
		int xa = anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0;
		int ya = anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0;
		aabb = new AABB(
				x + xa,
				y + ya,
				x + xa + width,
				y + ya + height);
	}

	public Button(String text, int x, int y, int padding, int fontSize, boolean centered,String anchor) {
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
	public void setClickEvent(IEvent clickEvent){
		this.clickEvent = clickEvent;
	}

	@Override
	public boolean mouseClick(int x, int y, int button) {
		if(aabb.contains(x,y)){
			clickEvent.trigger();
			return true;
		}
		return false;
	}

	@Override
	public void render(Graphics g,int offsetX, int offsetY) {
		if(sprite != null)
			sprite.draw(g,aabb.min.getIntX(), aabb.min.getIntY(), aabb.max.getIntX(),aabb.max.getIntY());
		else if(textImage != null){
			g.drawImage(textImage, aabb.min.getIntX(),aabb.min.getIntY(),null);
		}
	}

	@Override
	public void mouseRelease(int x, int y, int button) {

	}
}
