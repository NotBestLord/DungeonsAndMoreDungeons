package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.Vector2f;

import java.awt.*;

public class PopText extends GuiElement {
	private final Font font;
	private final Color textColor;
	public String string;
	private float x,y;
	private final boolean centered;
	private final Vector2f velocity;
	private final float duration;
	/**
	 * @param anchor "center"=x,y is offset from middle of screen,""=x,y is (x,y)
	 */
	public PopText(Font font, Color textColor, String string, float x, float y, float velocityMul,float duration, boolean centered, String anchor) {
		this.font = font;
		this.textColor = textColor;
		this.string = string;
		x += (anchor.equals("center_x") || anchor.equals("center") ? com.notlord.Window.WIDTH / 2f : 0);
		y += (anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2f : 0);
		x -= (centered ? (int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getWidth() / 2f: 0);
		y += (centered ? (int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getHeight() / 2f :
				(int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getHeight()) - 4;
		this.x = x;
		this.y = y;
		this.velocity = new Vector2f((float) (Math.random()*360f));
		velocity.mul(velocityMul);
		this.centered = centered;
		this.duration = duration;
	}

	/**
	 * @param anchor "center"=x,y is offset from middle of screen,""=x,y is (x,y)
	 */
	public PopText(Font font, Color textColor, String string, float x, float y, Vector2f velocity,float duration, boolean centered, String anchor) {
		this.font = font;
		this.textColor = textColor;
		this.string = string;
		x += (anchor.equals("center_x") || anchor.equals("center") ? com.notlord.Window.WIDTH / 2f : 0);
		y += (anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2f : 0);
		x -= (centered ? (int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getWidth() / 2f: 0);
		y += (centered ? (int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getHeight() / 2f :
				(int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getHeight()) - 4;
		this.x = x;
		this.y = y;
		this.velocity = velocity;
		this.centered = centered;
		this.duration = duration;
	}
	@Override
	public void render(Graphics g,int offsetX, int offsetY) {
		g.setFont(font);
		g.setColor(textColor);
		g.drawString(string,(int) x+offsetX, (int) y+offsetY);
	}

	private float t = 0;
	@Override
	public void nextTick(float deltaTime) {
		x += velocity.x * deltaTime;
		y += velocity.y * deltaTime;
		velocity.x -= (deltaTime * Math.signum(velocity.x));
		velocity.y -= (deltaTime * Math.signum(velocity.y));
		t += deltaTime;
	}

	@Override
	public boolean remove() {
		return t >= duration;
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.GUI_2;
	}

	public Font getFont() {
		return font;
	}

	public Color getTextColor() {
		return textColor;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public boolean isCentered() {
		return centered;
	}

	public Vector2f getVelocity() {
		return velocity;
	}

	public float getDuration() {
		return duration;
	}
}
