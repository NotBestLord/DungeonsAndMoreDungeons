package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;

import java.awt.*;

public class Label implements IRender {
	private final Font font;
	private final Color textColor;
	public String string;
	private final int x,y;
	private final boolean centered;
	/**
	 * @param anchor "center"=x,y is offset from middle of screen,""=x,y is (x,y)
	 */
	public Label(Font font, Color textColor, String string, int x, int y, boolean centered,String anchor) {
		this.font = font;
		this.textColor = textColor;
		this.string = string;
		x += (anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0);
		y += (anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0);
		x -= (centered ? (int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getWidth() / 2: 0);
		y += (centered ? (int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getHeight() / 2 :
				(int) Constants.DEBUG_CANVAS.getFontMetrics(font).getStringBounds(string,null).getHeight()) - 4;
		this.x = x;
		this.y = y;
		this.centered = centered;
	}

	@Override
	public void render(Graphics g,int offsetX, int offsetY) {
		g.setFont(font);
		g.setColor(textColor);
		g.drawString(string,x,y);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.GUI;
	}
}
