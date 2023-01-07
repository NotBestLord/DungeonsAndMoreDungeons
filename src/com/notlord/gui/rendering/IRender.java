package com.notlord.gui.rendering;

import java.awt.*;

public interface IRender {
	void render(Graphics g, int offsetX,int offsetY);
	RenderPriority getPriority();
}
