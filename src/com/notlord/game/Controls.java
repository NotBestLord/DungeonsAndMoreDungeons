package com.notlord.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controls {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final ControlsSave SAVE;
	private static final File file = new File("config/controls.json");
	static{
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ControlsSave temp = null;
		try {
			temp = GSON.fromJson(new FileReader(file), ControlsSave.class);
		} catch (FileNotFoundException ignored) {
			System.out.println("Controls File Not Found");
		}

		if(temp != null) {
			SAVE = temp;
		}
		else{
			SAVE = new ControlsSave();
			saveControls();
		}
	}

	private static void saveControls(){
		if(!file.exists()) return;
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(GSON.toJson(SAVE));
			writer.flush();
			writer.close();
		}
		catch (IOException ignored){
			System.out.println("Failed to save controls");
		}
	}

	public static String getKey(int keyCode){
		return SAVE.keys.getOrDefault(keyCode, "NULL");
	}

	public static int getKeyCode(String key){
		return SAVE.keyCodes.getOrDefault(key, -1);
	}

	public static void setKey(String key, int keyCode){
		if(SAVE.keyCodes.containsKey(key)){
			SAVE.keys.remove(SAVE.keyCodes.get(key));
			SAVE.reg(keyCode,key);
			saveControls();
		}
	}

	public static List<String> getAllKeys(){
		return new ArrayList<>(SAVE.keys.values());
	}

	public static String getKeyLabel(int keyCode){
		return KeyEvent.getKeyText(keyCode);
	}

	private static class ControlsSave{
		public final Map<String, Integer> keyCodes = new HashMap<>();
		public final Map<Integer, String> keys = new HashMap<>();

		public ControlsSave() {
			reg(KeyEvent.VK_W, "move_up");
			reg(KeyEvent.VK_A, "move_left");
			reg(KeyEvent.VK_S, "move_down");
			reg(KeyEvent.VK_D, "move_right");
			reg(KeyEvent.VK_SHIFT,"sprint");
			reg(KeyEvent.VK_ESCAPE, "pause");
			reg(KeyEvent.VK_SPACE, "dash");
			reg(KeyEvent.VK_K, "ability_tree");
			reg(KeyEvent.VK_1, "ability_1");
			reg(KeyEvent.VK_2, "ability_2");
			reg(KeyEvent.VK_3, "ability_3");
			reg(KeyEvent.VK_4, "ability_4");
			reg(KeyEvent.VK_5, "ability_5");
		}

		private void reg(int keyCode, String key){
			keys.put(keyCode,key);
			keyCodes.put(key,keyCode);
		}
	}
}
