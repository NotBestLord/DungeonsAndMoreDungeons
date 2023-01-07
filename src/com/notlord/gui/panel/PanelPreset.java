package com.notlord.gui.panel;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.Controls;
import com.notlord.gui.images.ImageRender;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.interaction.Button;
import com.notlord.gui.interaction.Label;
import com.notlord.gui.interaction.*;
import com.notlord.gui.rendering.RenderPriority;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelPreset {
	public static final PanelPreset MAIN_MENU = new PanelPreset(),MAIN_MENU_HOST = new PanelPreset(),MAIN_MENU_JOIN = new PanelPreset();
	public static PanelPreset SETTINGS_MENU = new PanelPreset();
	public static PanelPreset CONTROLS_SETTINGS_MENU = new PanelPreset();
	public static PanelPreset PAUSE_MENU_CLIENT = new PanelPreset();
	public static PanelPreset PAUSE_MENU_SERVER = new PanelPreset();

	static{
		Label label = new Label(Constants.DEFAULT_FONT, Color.black, "Lord's Game:", 0,10, true,"center_x");
		Button button1 = new Button("Host", 0,120,8,24,true,"center_x");
		button1.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(MAIN_MENU_HOST.objects());
				});
		Button button2 = new Button("Join", 0,200,8,24,true,"center_x");
		button2.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(MAIN_MENU_JOIN.objects());
				});
		Button button3 = new Button("Settings", 0,280,8,24,true,"center_x");
		button3.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(SETTINGS_MENU.objects());
				});
		Button button4 = new Button("Exit", 0,360,8,24,true,"center_x");
		button4.setClickEvent(() -> Main.windowOpen = false);
		MAIN_MENU.objects.addAll(List.of(label,button1,button2,button3,button4));
	}
	static{
		Label label = new Label(Constants.DEFAULT_FONT, Color.black, "Server IP:", -120,50, true,"center_x");
		TextBox hostInput = new TextBox(-60,50,32,new Color(255,255,200), Color.black,"center_x");
		hostInput.getLabel().string = "localhost";
		Label label2 = new Label(Constants.DEFAULT_FONT, Color.black, "Server Port:", -60,100, true,"center_x");
		TextBox portInput = new TextBox(0,100,20,new Color(255,255,200), Color.black,"center_x");
		portInput.getLabel().string = "7777";
		Label err = new Label(Constants.DEFAULT_FONT, Color.red, "", 0,200, true,"center_x");
		Button button = new Button("Connect",0,150,8,24,true,"center_x");
		button.setClickEvent(
				() -> {
					try {
						Main.clientHandler.beginConnection(hostInput.getLabel().string,Integer.parseInt(portInput.getLabel().string));
						err.string ="";
					}
					catch (NumberFormatException e){
						err.string = "Invalid Port";
					}
					catch (Exception e){
						err.string = "Bad IP";
					}
				});
		OptionsButton optionsButton = new OptionsButton(Color.LIGHT_GRAY,0,240,180,20,true,"center_x",
				"Class:Mage","Class:Knight","Class:Ranger");
		optionsButton.setListener(value -> Constants.joinAbilityTreeType = value.split(":")[1].toLowerCase());
		Button back = new Button(new Sprite("gui_back_button"), 0,0,80,80,"");
		back.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(MAIN_MENU.objects());
				});
		MAIN_MENU_JOIN.objects.addAll(List.of(label, hostInput, label2, portInput,button,optionsButton,back,err));
	}
	static{
		Label label = new Label(Constants.DEFAULT_FONT, Color.black, "Port:", -60,50, true,"center_x");
		TextBox portInput = new TextBox(0,50,20,new Color(255,255,200), Color.black,"center_x");
		portInput.getLabel().string = "7777";
		Label err = new Label(Constants.DEFAULT_FONT, Color.red, "", 0,200, true,"center_x");
		Button button = new Button("Start",0,100,8,24,true,"center_x");
		button.setClickEvent(() -> {
			try {
				Main.serverHandler.server.setPort(Integer.parseInt(portInput.getLabel().string));
				Main.serverHandler.start();
				err.string ="";
				Main.getPanel().clearObjects();
			}
			catch (NumberFormatException e){
				err.string = "Invalid Port";
			}
		});
		OptionsButton optionsButton = new OptionsButton(Color.LIGHT_GRAY,0,240,180,20,true,"center_x",
				"Class:Mage","Class:Knight","Class:Ranger");
		optionsButton.setListener(value -> Constants.hostAbilityTreeType = value.split(":")[1].toLowerCase());
		Button back = new Button(new Sprite("gui_back_button"), 0,0,80,80,"");
		back.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(MAIN_MENU.objects());
				});
		MAIN_MENU_HOST.objects.addAll(List.of(label,portInput,button,optionsButton,back,err));
	}
	static{
		Label label = new Label(Constants.DEFAULT_FONT, Color.black, "Settings:",0,20,true,"center_x");
		OptionsButton optionsButton = new OptionsButton(Color.LIGHT_GRAY,0,80,120,20,true,"center_x","FPS:60","FPS:120","FPS:30");
		optionsButton.setListener(value -> Constants.PANEL_FPS = Double.parseDouble(optionsButton.getValue().split(":")[1]));
		//
		Button fullscreen = new Button("Fullscreen (Not Work)",0,120,8,20,true,"center_x");
		fullscreen.setClickEvent(() -> {
			/*Window window = Main.getWindow();
			if(window.getExtendedState() == Window.MAXIMIZED_BOTH){
				window.setVisible(false);
				window.setExtendedState(Window.NORMAL);
				window.dispose();
				window.setUndecorated(false);
				window.setSize(Window.WIDTH, Window.HEIGHT);
				window.setVisible(true);
			}
			else{
				window.setVisible(false);
				window.dispose();
				window.setExtendedState(Window.MAXIMIZED_BOTH);
				window.setUndecorated(true);
				window.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration().getBounds().width,
						GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration().getBounds().height);
				window.setVisible(true);
			}*/
		});
		Button button = new Button("Control Settings",0,200,8,24,true,"center_x");
		button.setClickEvent(() -> {
			Main.getPanel().clearObjects();
			Main.getPanel().addObject(CONTROLS_SETTINGS_MENU.objects());
		});
		Button back = new Button(new Sprite("gui_back_button"), 0,0,80,80,"");
		back.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(MAIN_MENU.objects());
				});
		SETTINGS_MENU.objects.addAll(List.of(label,optionsButton,fullscreen,button,back));
	}
	static{
		int y = 40;
		Font font = new Font(Constants.DEFAULT_FONT.getName(), Constants.DEFAULT_FONT.getStyle(), 20);
		for(String key : Controls.getAllKeys()){
			Label label = new Label(font,Color.black,Controls.getKeyLabel(Controls.getKeyCode(key)),50,y,true,"center_x");
			KeyCaptureButton button = new KeyCaptureButton(key.toUpperCase(Locale.ROOT),-50,y,8,20,true,"center_x");
			button.setFocusEvent(focus -> {
				if(focus){
					label.string = "<"+Controls.getKeyLabel(Controls.getKeyCode(key))+">";
				}
				else{
					label.string = Controls.getKeyLabel(Controls.getKeyCode(key));
				}
			});
			button.setInputEvent( keyCode -> {
				Controls.setKey(key, keyCode);
				label.string = Controls.getKeyLabel(keyCode);
			});
			CONTROLS_SETTINGS_MENU.objects.add(label);
			CONTROLS_SETTINGS_MENU.objects.add(button);
			y+=40;
		}
		Button back = new Button(new Sprite("gui_back_button"), 0,0,80,80,"");
		back.setClickEvent(
				() -> {
					Main.getPanel().clearObjects();
					Main.getPanel().addObject(SETTINGS_MENU.objects());
				});
		CONTROLS_SETTINGS_MENU.objects.add(back);
	}
	static{
		ImageRender imageRender = new ImageRender(new Color(100,100,145,100), RenderPriority.PAUSE_MENU);
		Button exitButton = new Button("Exit To Main Menu", 0,240,8,24,true,"center_x");
		exitButton.setClickEvent(() -> {
			if(Main.clientHandler.client.isConnected()){
				Main.clientHandler.client.close();
				Main.clientHandler.getPanelLogic().pauseMenuOpen = false;
				Main.getPanel().removeObject(PAUSE_MENU_CLIENT.objects());
			}
		});
		exitButton.setRenderPriority(RenderPriority.PAUSE_MENU_GUI);
		Button exitButton2 = new Button("Exit To Desktop", 0,360,8,24,true,"center_x");
		exitButton2.setClickEvent(() -> Main.windowOpen = false);
		exitButton2.setRenderPriority(RenderPriority.PAUSE_MENU_GUI);
		PAUSE_MENU_CLIENT.objects.addAll(List.of(imageRender,exitButton,exitButton2));
	}
	static{
		ImageRender imageRender = new ImageRender(new Color(100,100,145,100), RenderPriority.PAUSE_MENU);
		Button db = new Button("Reroll Dungeon", 0,120,8,24,true,"center_x");
		db.setClickEvent(() -> Main.serverHandler.generateNewDungeon());
		db.setRenderPriority(RenderPriority.PAUSE_MENU_GUI);
		Label label = new Label(new Font(Constants.DEFAULT_FONT.getName(), Constants.DEFAULT_FONT.getStyle(), 20)
				,Color.black,"(Use Only When Dungeon Is Not Clearable)",0,160,true,"center_x");
		Label label2 = new Label(new Font(Constants.DEFAULT_FONT.getName(), Constants.DEFAULT_FONT.getStyle(), 20)
				,Color.black,"(Rerolling a dungeon will not give you rewards)",0,180,true,"center_x");
		Button exitButton = new Button("Exit To Main Menu", 0,240,8,24,true,"center_x");
		exitButton.setClickEvent(() -> {
			if(Main.serverHandler.server.isRunning()){
				Main.serverHandler.server.close();
				Main.serverHandler.getPanelLogic().pauseMenuOpen = false;
				Main.getPanel().removeObject(PAUSE_MENU_SERVER.objects());
			}
		});
		exitButton.setRenderPriority(RenderPriority.PAUSE_MENU_GUI);
		Button exitButton2 = new Button("Exit To Desktop", 0,360,8,24,true,"center_x");
		exitButton2.setClickEvent(() -> Main.windowOpen = false);
		exitButton2.setRenderPriority(RenderPriority.PAUSE_MENU_GUI);
		PAUSE_MENU_SERVER.objects.addAll(List.of(imageRender,exitButton,exitButton2,db,label,label2));
	}


	//
	private final List<Object> objects = new ArrayList<>();
	public Object[] objects(){
		return objects.toArray();
	}
}
