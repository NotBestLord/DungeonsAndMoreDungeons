package com.notlord.gui.panel;

import com.notlord.ClientHandler;
import com.notlord.Main;
import com.notlord.Window;
import com.notlord.game.Controls;
import com.notlord.game.client.ClientPlayer;
import com.notlord.gui.interaction.IFocus;
import com.notlord.gui.interaction.IGuiClick;
import com.notlord.networking.packets.InputPacket;
import com.notlord.networking.packets.MouseInputPacket;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PanelClientLogic implements MouseListener, KeyListener {
	private final ClientHandler clientHandle;
	private final Panel panel = Main.getPanel();
	public boolean pauseMenuOpen = false;
	public PanelClientLogic(ClientHandler clientHandle) {
		this.clientHandle = clientHandle;
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

	public void enable(){
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
				clientHandle.client.send(new InputPacket("clear", 0));
				heldDownChars.clear();
			}
		});
		panel.setBackground(new Color(33,30,39));
	}

	public void registerPlayer(ClientPlayer player){
		panel.renderEngine.setOffsetPoint(player);
	}

	/*@Override
	public void run() {
		// handle XInput
		XInputDevice device = null;
		try {
			device = XInputDevice.getDeviceFor(0);
		} catch (XInputNotLoadedException e) {
			e.printStackTrace();
		}
		if(device == null) return;
		device.addListener(this);
		while (!device.isConnected()) {
			Thread.onSpinWait();
		}
		while (device.isConnected() && clientHandle.isConnected()){
			device.poll();
		}
	}*/

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		AtomicBoolean guiClick = new AtomicBoolean(false);
		int x = e.getX() - 7;
		int y = e.getY() - 29;
		new ArrayList<>(panel.objectsInScreen).forEach(o -> {
			if(o instanceof IGuiClick t){
				guiClick.set(t.mouseClick(x,y, e.getButton()) || guiClick.get());
			}
		});
		if(guiClick.get()){
			// send end input packets of all held down keys
			if(clientHandle.isConnected()) {
				for (int c : heldDownChars) {
					if(!Controls.getKey(c).equals("NULL"))
						clientHandle.sendPacket(new InputPacket(Controls.getKey(c), 1));
				}
			}
			heldDownChars.clear();
		}
		else if(!pauseMenuOpen && !guiClick.get() && clientHandle.isConnected() && clientHandle.getPlayer() != null){
			// send projectile packet
			clientHandle.client.send(new MouseInputPacket(x + clientHandle.getPlayer().getIntX() - (Window.WIDTH/2),
					y + clientHandle.getPlayer().getIntY() - (Window.HEIGHT/2), e.getButton()));
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

	public final List<Integer> heldDownChars = new CopyOnWriteArrayList<>();

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
		if(!pauseMenuOpen && !guiKey.get() && !heldDownChars.contains(e.getKeyCode())){
			// send packet to server
			if(clientHandle.isConnected() && !Controls.getKey(e.getKeyCode()).equals("NULL")) {
				clientHandle.sendPacket(new InputPacket(Controls.getKey(e.getKeyCode()), 0));
			}
			heldDownChars.add(e.getKeyCode());
		}
		if(e.getKeyCode() == Controls.getKeyCode("pause")){
			if(pauseMenuOpen){
				panel.removeObject(PanelPreset.PAUSE_MENU_CLIENT.objects());
			}
			else{
				panel.addObject(PanelPreset.PAUSE_MENU_CLIENT.objects());
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
		if(heldDownChars.remove((Integer) e.getKeyCode()) && clientHandle.isConnected() && !Controls.getKey(e.getKeyCode()).equals("NULL")){
			// send end input packet
			clientHandle.sendPacket(new InputPacket(Controls.getKey(e.getKeyCode()), 1));
		}
	}
}
