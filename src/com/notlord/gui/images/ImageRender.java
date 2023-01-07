package com.notlord.gui.images;

import com.notlord.Main;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageRender implements IRender {
	private final BufferedImage image;
	private final RenderPriority priority;
	public ImageRender(String key, RenderPriority priority){
		image = SpriteUtils.getImage(key);
		this.priority = priority;
	}
	public ImageRender(Color bgColor, RenderPriority priority){
		image = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(bgColor);
		g.fillRect(0,0,16,16);
		this.priority = priority;
	}
	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.drawImage(image,0,0,Main.getWindow().getWidth(), Main.getWindow().getHeight(), null);
	}

	@Override
	public RenderPriority getPriority() {
		return priority;
	}
}
