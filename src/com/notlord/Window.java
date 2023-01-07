package com.notlord;

import com.notlord.gui.panel.Panel;

import javax.swing.*;

public class Window extends JFrame {
	public static final int WIDTH = 1280, HEIGHT = 720;
	private final Panel panel;
	public Window(String title, Panel panel) {
		super(title);
		setResizable(false);
		setLayout(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//
		this.panel = panel;
		add(panel);
		panel.setBounds(0,0,WIDTH,HEIGHT);
		//
		setSize(WIDTH+16, HEIGHT+39);
		//
		addKeyListener(panel);
	}

	@Override
	public void setSize(int width, int height){
		super.setSize(width,height);
		panel.setBounds(0,0, width, height);
	}

	public void drawFrame(float dt){
		panel.nextTick(dt);
		panel.repaint();
	}
}
