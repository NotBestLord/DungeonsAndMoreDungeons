package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.gui.interaction.listeners.SlideBarListener;
import com.notlord.math.AABB;

import java.awt.*;

public class SlideBar extends GuiElement implements IGuiClick {
	private float value;
	private final int x,y,width,height;
	private final Color barColor;
	private SlideBarListener listener;
	public SlideBar(float def_val, int x, int y, int width, int height, boolean centered,String anchor, Color barColor){
		this.value = def_val;
		this.x = x + (anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0) - (centered ? width / 2 : 0);
		this.y = y + (anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0) - (centered ? height / 2 : 0);
		this.width = width;
		this.height = height;
		this.barColor = barColor;
	}

	public void setListener(SlideBarListener listener) {
		this.listener = listener;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.setColor(barColor);
		g.fillRect(x,y,width,height);
		g.setColor(Color.gray);
		g.fillRect((int) ((int) (x + (width * value)) - (height*1.2f/2)), y, (int) (height * 1.2f), (int) (height * 1.2f));
		String v = "" + ((int) (value*100));
		g.setColor(Color.black);
		g.setFont(Constants.DEFAULT_FONT);
		g.drawString(v,x+width+5, (int) (y + (g.getFontMetrics(Constants.DEFAULT_FONT).getStringBounds(v,g).getHeight()/2)));
	}

	private boolean capture = false;

	@Override
	public boolean mouseClick(int mx, int my, int button) {
		if(new AABB(x,y,x+width, y+height).contains(mx,my) && button == 1){
			capture = true;
			return true;
		}
		return false;
	}

	@Override
	public void mouseRelease(int mx, int my, int button) {
		if(button == 1 && capture){
			this.value = Math.max(0f,Math.min(1f, (mx - x) / (float) width));
			listener.onValueChange(value);
			capture = false;
		}
	}
}
