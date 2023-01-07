package com.notlord.gui.rendering;

import com.notlord.Window;
import com.notlord.game.IPosition;
import com.notlord.gui.panel.Panel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RenderEngine {
	private final Map<IRender, RenderPriority> renderPriority = new ConcurrentHashMap<>();
	private final Map<RenderPriority, List<IRender>> toRenderSorted = new ConcurrentHashMap<>();
	private final Panel parentPanel;
	private IPosition offsetPoint = null;
	public RenderEngine(Panel panel) {
		for(RenderPriority priority : RenderPriority.values()){
			toRenderSorted.put(priority, new ArrayList<>());
		}
		this.parentPanel = panel;
	}
	public void setOffsetPoint(IPosition iPosition){
		offsetPoint = iPosition;
	}
	public void render(Graphics g){
		BufferedImage image = new BufferedImage(parentPanel.getWidth(),parentPanel.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(parentPanel.getBackground());
		int offsetX=0,offsetY=0;
		if(offsetPoint != null) {
			offsetX = (Window.WIDTH / 2) - offsetPoint.getIntX();
			offsetY = (Window.HEIGHT / 2) - offsetPoint.getIntY();
		}
		graphics.fillRect(0,0,parentPanel.getWidth(), parentPanel.getHeight());
		for(RenderPriority priority : RenderPriority.values()){
			if(priority != RenderPriority.NORMAL) {
				for (IRender renderEnabled : new ArrayList<>(toRenderSorted.get(priority))) {
					renderEnabled.render(graphics,offsetX,offsetY);
				}
			}
			else{
				Map<IRender, Float> poses = new HashMap<>();
				int l = toRenderSorted.get(priority).size();
				float[] yPoses = new float[l];
				for(int i=0;i<l;i++){
					if(toRenderSorted.get(priority).get(i) instanceof IPosition position) {
						yPoses[i] = position.getY();
						poses.put(toRenderSorted.get(priority).get(i), yPoses[i]);
					}
				}
				Arrays.sort(yPoses);
				for (float y : yPoses) {
					for (IRender render : new ArrayList<>(poses.keySet())) {
						if (poses.get(render) == y){
							render.render(graphics, offsetX, offsetY);
							poses.remove(render);
						}
					}
				}
			}
		}
		g.drawImage(image.getSubimage(0,0,Window.WIDTH+16,Window.HEIGHT+39),
				0,0, parentPanel.getWidth(), parentPanel.getHeight(),null);
	}

	public void add(IRender render, RenderPriority priority){
		toRenderSorted.get(priority).add(render);
		renderPriority.put(render, priority);
	}

	public void remove(IRender render){
		if(renderPriority.get(render) != null) {
			toRenderSorted.get(renderPriority.get(render)).remove(render);
		}
		renderPriority.remove(render);
	}

	public void removeAll(List<IRender> renders){
		renders.forEach(this::remove);
	}
}
