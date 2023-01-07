package com.notlord.gui.rendering;

import java.awt.*;

public interface IDraw {
	void setRotation(float angle);
	void draw(Graphics g, int dx, int dy);
	void draw(Graphics g, int dx1, int dy1, int dx2, int dy2);
}
