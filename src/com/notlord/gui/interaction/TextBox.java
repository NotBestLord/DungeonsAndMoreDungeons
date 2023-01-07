package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TextBox extends GuiElement implements IGuiClick, KeyListener, IFocus {
	private final Label label;
	private final AABB aabb;
	private final Color bgColor;
	private final Color textColor;
	private final int maxLength;
	private boolean focused = false;
	private final String anchor;
	/**
	 * @param anchor "center"=x,y is offset from middle of screen,""=x,y is (x,y)
	 */
	public TextBox(int x, int y, int maxLength, Color bgColor,Color textColor,String anchor) {
		this.bgColor = bgColor;
		this.textColor = textColor;
		this.maxLength = maxLength;
		int width = (int) Constants.DEBUG_CANVAS.getFontMetrics(Constants.DEFAULT_FONT).getStringBounds("w".repeat(maxLength+1), Constants.DEBUG_CANVAS.getGraphics()).getWidth();
		int height = (int) Constants.DEBUG_CANVAS.getFontMetrics(Constants.DEFAULT_FONT).getStringBounds("w".repeat(maxLength+1), Constants.DEBUG_CANVAS.getGraphics()).getHeight();
		int xa = anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0;
		int ya = anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0;
		aabb = new AABB(
				x + xa,
				y + ya,
				x + xa + width,
				y + ya + height);
		label = new Label(Constants.DEFAULT_FONT, textColor, "",x,y,false, anchor);
		this.anchor = anchor;

	}

	@Override
	public boolean mouseClick(int x, int y, int button) {
		if(aabb.contains(x,y)){
			focused = true;
			return true;
		}
		focused = false;
		addL = false;
		return false;
	}

	@Override
	public void mouseRelease(int x, int y, int button) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if(focused){
			if((int) e.getKeyChar() == KeyEvent.VK_BACK_SPACE){
				if(label.string.length() != 0) {
					label.string = label.string.substring(0, label.string.length() - 1);
				}
			}
			else if(Constants.TEXT_BOX_KEYS.contains((e.getKeyChar()+"").toLowerCase().toCharArray()[0])){
				if(label.string.length() < maxLength){
					label.string = label.string + e.getKeyChar();

				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void render(Graphics g,int offsetX, int offsetY) {
		g.setColor(bgColor);
		g.fillRect(aabb.min.getIntX(), aabb.min.getIntY(), aabb.getWidthInt(), aabb.getHeightInt());
		g.setColor(textColor);
		label.render(g,offsetX, offsetY);
		if(addL) {
			g.drawString("|", (int) (aabb.min.getIntX() +
							g.getFontMetrics(Constants.DEFAULT_FONT).getStringBounds(label.string, g).getWidth()),
					aabb.max.getIntY() - 4);
		}
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.GUI;
	}
	private float tick = 0;
	private boolean addL = false;
	@Override
	public void nextTick(float deltaTime) {
		if(!focused) return;
		tick += deltaTime;
		if(tick >= 0.5f){
			if(!addL){
				if(label.string.length() < maxLength) {
					addL = true;
				}
			}
			else{
				addL = false;
			}
			while (tick >= 0.5f){
				tick -= 0.5f;
			}
		}
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void unFocus() {
		focused = false;
	}

	public Label getLabel(){
		return label;
	}
}
