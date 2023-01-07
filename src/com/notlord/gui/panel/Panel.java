package com.notlord.gui.panel;

import com.notlord.game.ID;
import com.notlord.game.ITick;
import com.notlord.gui.images.IAnimationTick;
import com.notlord.gui.interaction.GuiElement;
import com.notlord.gui.interaction.IGuiClick;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Panel extends JPanel implements MouseListener, KeyListener, ITick {
	protected final RenderEngine renderEngine;
	protected final List<Object> objectsInScreen = new CopyOnWriteArrayList<>();
	private Color bg = Color.white;
	public Panel() {
		setDoubleBuffered(true);
		renderEngine = new RenderEngine(this);
	}

	@Override
	public void paint(Graphics g) {
		try {
			drawFrame(g);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void nextTick(float dt){
		List<GuiElement> lst = new ArrayList<>();
		objectsInScreen.forEach(o -> {
			if(o instanceof GuiElement g){
				g.nextTick(dt);
				if(g.remove()){
					lst.add(g);
				}
			}
			if(o instanceof IAnimationTick t){
				t.nextAnimationTick(dt);
			}
		});
		objectsInScreen.removeAll(lst);
		renderEngine.removeAll(new ArrayList<>(lst));
	}

	@Override
	public void setBackground(Color bg) {
		this.bg = bg;
	}

	@Override
	public Color getBackground() {
		return bg;
	}

	public void drawFrame(Graphics g) throws IOException {
		g.setColor(bg);
		g.fillRect(0,0,getWidth(),getHeight());
		renderEngine.render(g);
	}

	public void addObject(Object o) {
		objectsInScreen.add(o);
		if(o instanceof IRender r) {
			renderEngine.add(r, r.getPriority());
		}
	}
	public void addObject(Object... o) {
		objectsInScreen.addAll(List.of(o));
		for(Object o1 : o) {
			if (o1 instanceof IRender r) {
				renderEngine.add(r, r.getPriority());
			}
		}
	}
	public void removeObject(Object o) {
		objectsInScreen.remove(o);
		if(o instanceof IRender r) {
			renderEngine.remove(r);
		}
	}
	public void removeObject(Object... o) {
		objectsInScreen.removeAll(List.of(o));
		for(Object o1 : o) {
			if (o1 instanceof IRender r) {
				renderEngine.remove(r);
			}
		}
	}
	public void clearObjects(){
		for(Object o1 : objectsInScreen) {
			if (o1 instanceof IRender r) {
				renderEngine.remove(r);
			}
		}
		objectsInScreen.clear();
	}
	public void removeObjectWithId(String id){
		for(Object o1 : new ArrayList<>(objectsInScreen)) {
			if (o1 instanceof ID i) {
				if(i.getId().equals(id)){
					if(o1 instanceof IRender) {
						renderEngine.remove((IRender) o1);
					}
					objectsInScreen.remove(o1);
					return;
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX() - 7;
		int y = e.getY() - 29;
		objectsInScreen.forEach(o -> {
			if(o instanceof IGuiClick t){
				t.mouseClick(x, y, e.getButton());
			}
		});
	}

	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX() - 7;
		int y = e.getY() - 29;
		new ArrayList<>(objectsInScreen).forEach(o -> {
			if(o instanceof IGuiClick t){
				t.mouseRelease(x,y, e.getButton());
			}
		});
	}
	@Override
	public void mouseEntered(MouseEvent e) {

	}
	@Override
	public void mouseExited(MouseEvent e) {

	}


	@Override
	public void keyTyped(KeyEvent e) {
		objectsInScreen.forEach(o -> {
			if(o instanceof KeyListener k){
				k.keyTyped(e);
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		objectsInScreen.forEach(o -> {
			if(o instanceof KeyListener k){
				k.keyPressed(e);
			}
		});
	}

	@Override
	public void keyReleased(KeyEvent e) {
		objectsInScreen.forEach(o -> {
			if(o instanceof KeyListener k){
				k.keyReleased(e);
			}
		});
	}
}
