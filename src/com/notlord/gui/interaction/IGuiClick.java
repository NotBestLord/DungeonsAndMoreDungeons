package com.notlord.gui.interaction;

public interface IGuiClick {
	boolean mouseClick(int x, int y, int button);
	void mouseRelease(int x, int y, int button);
}
