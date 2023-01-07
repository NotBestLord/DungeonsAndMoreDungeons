package com.notlord.gui.interaction;

import com.notlord.Constants;
import com.notlord.Window;
import com.notlord.gui.interaction.listeners.OptionsButtonListener;
import com.notlord.math.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OptionsButton extends GuiElement implements IGuiClick {
	private final Font font = Constants.DEFAULT_FONT;
	private final Color buttonColor;
	private final List<String> values = new ArrayList<>();
	private int pointer = 0;
	private final int x,y,width,height;
	private OptionsButtonListener listener;
	public OptionsButton(Color buttonColor, int x, int y, int width, int height, boolean centered, String anchor, String... values) {
		this.buttonColor = buttonColor;
		this.width = width;
		this.height = height;
		this.values.addAll(List.of(values));
		this.x = x + (anchor.equals("center_x") || anchor.equals("center") ? Window.WIDTH / 2 : 0) - (centered ? width / 2 : 0);
		this.y = y + (anchor.equals("center_y") || anchor.equals("center") ? Window.HEIGHT / 2 : 0) - (centered ? height / 2 : 0);
	}

	public void setListener(OptionsButtonListener listener) {
		this.listener = listener;
	}
	public String getValue(){
		return values.get(pointer);
	}
	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.setColor(buttonColor);
		g.fillRect(x, y, width, height);
		g.setFont(font);
		g.setColor(Color.black);
		int sw = (int) g.getFontMetrics(font).getStringBounds("<",g).getWidth();
		int sh = (int) g.getFontMetrics(font).getStringBounds("<",g).getHeight();
		int w = ((width / 5) - sw) / 2;
		int h = ((height / 5) - sh) / 2;
		g.drawString("<",x+w,y-h*2);
		g.drawString(">",x+w+(width*4/5),y-h*2);
		int sw2 = (int) g.getFontMetrics(font).getStringBounds(values.get(pointer),g).getWidth();
		int sh2 = (int) g.getFontMetrics(font).getStringBounds("|",g).getHeight();
		int w2 = ((width / 5) - sw2) / 2;
		int h2 = ((height / 5) - sh2) / 2;
		g.drawString(values.get(pointer),x+w2+(width * 2 / 5),y-h2*2);
	}

	@Override
	public boolean mouseClick(int mx, int my, int button) {
		if(button != 1) return false;
		if(new AABB(x+((width*4)/5),y,x+width,y+height).contains(mx,my)){
			pointer++;
			if(pointer >= values.size()){
				pointer = 0;
			}
			listener.onValueChange(values.get(pointer));
			return true;
		}
		else if(new AABB(x,y,x+(width / 5),y+height).contains(mx,my)){
			pointer--;
			if(pointer < 0){
				pointer = values.size() - 1;
			}
			listener.onValueChange(values.get(pointer));
			return true;
		}
		return false;
	}

	@Override
	public void mouseRelease(int x, int y, int button) {

	}
}
