package com.notlord.gui.interaction;

import com.notlord.game.ITick;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;

import java.awt.*;

public class GuiElement implements ITick, IRender {
	private RenderPriority priority = RenderPriority.GUI;

	@Override
	public void nextTick(float deltaTime) {

	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {

	}

	@Override
	public RenderPriority getPriority() {
		return priority;
	}

	public void setRenderPriority(RenderPriority priority) {
		this.priority = priority;
	}

	public boolean remove(){ return false; }
}
