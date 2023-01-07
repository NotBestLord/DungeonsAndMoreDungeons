package com.notlord.gui.panel;

import com.notlord.Main;
import com.notlord.ServerHandler;
import com.notlord.Window;
import com.notlord.game.Controls;
import com.notlord.game.server.ServerPlayer;
import com.notlord.gui.interaction.IFocus;
import com.notlord.gui.interaction.IGuiClick;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PanelServerLogic implements MouseListener, KeyListener {
	private final ServerHandler serverHandler;
	private final Panel panel = Main.getPanel();
	public List<String> heldDownChars;
	public boolean pauseMenuOpen = false;
	public PanelServerLogic(ServerHandler serverHandler) {
		this.serverHandler = serverHandler;
	}

	public void enable(ServerPlayer player){
		heldDownChars = player.getHeldDownKeys();
		for (KeyListener listener : new ArrayList<>(List.of(Main.getWindow().getKeyListeners()))) {
			Main.getWindow().removeKeyListener(listener);
		}
		for (MouseListener listener : new ArrayList<>(List.of(Main.getWindow().getMouseListeners()))) {
			Main.getWindow().removeMouseListener(listener);
		}
		Main.getWindow().addMouseListener(this);
		Main.getWindow().addKeyListener(this);
		Main.getWindow().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				heldDownChars.clear();
			}
		});
		panel.setBackground(new Color(33,30,39));
		panel.renderEngine.setOffsetPoint(player);
		//serverHandler.addEntity(new Enemy(200,200,3,"player_purple"));
	}

	public void disable(){
		for (KeyListener listener : new ArrayList<>(List.of(Main.getWindow().getKeyListeners()))) {
			Main.getWindow().removeKeyListener(listener);
		}
		for (MouseListener listener : new ArrayList<>(List.of(Main.getWindow().getMouseListeners()))) {
			Main.getWindow().removeMouseListener(listener);
		}
		for (FocusListener listener : new ArrayList<>(List.of(Main.getWindow().getFocusListeners()))) {
			Main.getWindow().removeFocusListener(listener);
		}
		Main.getWindow().addMouseListener(panel);
		Main.getWindow().addKeyListener(panel);
		panel.setBackground(Color.white);
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		AtomicBoolean guiClick = new AtomicBoolean(false);
		int x = e.getX() - 7;
		int y = e.getY() - 29;
		AtomicBoolean focus = new AtomicBoolean(false);
		new ArrayList<>(panel.objectsInScreen).forEach(o -> {
			if(o instanceof IGuiClick t){
				guiClick.set(guiClick.get() || t.mouseClick(x, y, e.getButton()));
			}
			if(o instanceof IFocus f){
				focus.set(focus.get() || f.isFocused());
			}
		});
		if(guiClick.get()){
			heldDownChars.clear();
		}
		else if(!pauseMenuOpen && !focus.get() && !guiClick.get()){
			// send projectile packet
			serverHandler.getPlayer().handleMouseInput(x + serverHandler.getPlayer().getIntX() - (Window.WIDTH/2),
					y + serverHandler.getPlayer().getIntY() - (Window.HEIGHT/2), e.getButton());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX() - 7;
		int y = e.getY() - 29;
		new ArrayList<>(panel.objectsInScreen).forEach(o -> {
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
		new ArrayList<>(panel.objectsInScreen).forEach(o -> {
			if(o instanceof KeyListener k){
				k.keyTyped(e);
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		AtomicBoolean guiKey = new AtomicBoolean(false);
		new ArrayList<>(panel.objectsInScreen).forEach(o -> {
			if(o instanceof KeyListener k){
				k.keyPressed(e);
				if(o instanceof IFocus f){
					if(pauseMenuOpen && f.isFocused()) f.unFocus();
					else if (!pauseMenuOpen) guiKey.set(guiKey.get() || f.isFocused());
				}
			}
		});
		if(!pauseMenuOpen && !guiKey.get() && !Controls.getKey(e.getKeyCode()).equals("NULL") && !heldDownChars.contains(Controls.getKey(e.getKeyCode()))){
			// send packet to server
			heldDownChars.add(Controls.getKey(e.getKeyCode()));
		}
		if(e.getKeyCode() == Controls.getKeyCode("pause")){
			if(pauseMenuOpen){
				panel.removeObject(PanelPreset.PAUSE_MENU_SERVER.objects());
			}
			else{
				panel.addObject(PanelPreset.PAUSE_MENU_SERVER.objects());
			}
			pauseMenuOpen = !pauseMenuOpen;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		new ArrayList<>(panel.objectsInScreen).forEach(o -> {
			if(o instanceof KeyListener k){
				k.keyReleased(e);
			}
		});
		heldDownChars.remove(Controls.getKey(e.getKeyCode()));
	}
}
