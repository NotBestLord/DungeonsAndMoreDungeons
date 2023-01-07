package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.math.AABB;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class KeyCaptureButton extends GuiElement implements IGuiClick, KeyListener,IFocus {
	private final BufferedImage textImage;
	private Consumer<Boolean> focusEvent;
	private Consumer<Integer> inputEvent;
	private final AABB aabb;
	private boolean focus = false;

	public KeyCaptureButton(String text, int x, int y, int padding, int fontSize, boolean centered, String anchor) {
		int xa = anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0;
		int ya = anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0;
		Font font = new Font(Constants.DEFAULT_FONT.getName(), Constants.DEFAULT_FONT.getStyle(), fontSize);
		int width = (int) (padding * 2 +
				Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(text, Constants.DEBUG_CANVAS.getGraphics()).getWidth());
		int height = (int) (padding * 2 +
				Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(text, Constants.DEBUG_CANVAS.getGraphics()).getHeight());
		if (centered) {
			xa -= width / 2;
			ya -= height / 2;
		}
		this.textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = this.textImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.white);
		g.setFont(font);
		g.drawString(text, padding, height - padding * 2);
		aabb = new AABB(
				x + xa,
				y + ya,
				x + xa + width,
				y + ya + height);
	}

	public void setInputEvent(Consumer<Integer> inputEvent) {
		this.inputEvent = inputEvent;
	}

	public void setFocusEvent(Consumer<Boolean> focusEvent) {
		this.focusEvent = focusEvent;
	}

	@Override
	public boolean mouseClick(int x, int y, int button) {
		if (aabb.contains(x, y)) {
			focus = !focus;
			focusEvent.accept(focus);
			return true;
		}
		else{
			focus = false;
			focusEvent.accept(false);
		}
		return false;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.drawImage(textImage, aabb.min.getIntX(), aabb.min.getIntY(), null);
	}

	@Override
	public void mouseRelease(int x, int y, int button) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(focus){
			focus = false;
			inputEvent.accept(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public boolean isFocused() {
		return focus;
	}

	@Override
	public void unFocus() {
		focus = false;
	}
}
