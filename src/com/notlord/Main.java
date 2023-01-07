package com.notlord;

import com.notlord.gui.panel.Panel;
import com.notlord.gui.panel.PanelPreset;

import java.awt.event.WindowEvent;

public class Main {
	private static final Panel panel = new Panel();
	private static Window window;
	public static void main(String[] args) {
		panel.addObject(PanelPreset.MAIN_MENU.objects());
		window = new Window(Constants.TITLE, panel);
		window.addMouseListener(panel);
		window.setVisible(true);
		long last = System.nanoTime();
		double unprocessed = 0;
		while (windowOpen){
			long start = System.nanoTime();
			long passed = start - last;
			last = start;
			unprocessed += passed / (double) 1000000000;
			if(unprocessed >= 1 / Constants.PANEL_FPS) {
				window.drawFrame((float) unprocessed);
			}
			while (unprocessed >= 1 / Constants.PANEL_FPS){
				unprocessed -= 1 / Constants.PANEL_FPS;
			}
		}
		Main.getWindow().dispatchEvent(new WindowEvent(Main.getWindow(), WindowEvent.WINDOW_CLOSING));
	}

	public static Panel getPanel() {
		return panel;
	}
	public static Window getWindow() {
		return window;
	}
	public static ClientHandler clientHandler = new ClientHandler(panel);
	public static ServerHandler serverHandler = new ServerHandler(panel);
	public static boolean windowOpen = true;
	public static final Scheduler scheduler = new Scheduler();
}
