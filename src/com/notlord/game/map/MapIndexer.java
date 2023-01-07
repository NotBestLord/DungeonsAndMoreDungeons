package com.notlord.game.map;

import com.google.gson.Gson;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.utils.Tuple;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapIndexer {
	private static final Gson gson = new Gson();
	private static final Map<String, RoomData> rooms = new HashMap<>();
	private static final Map<String, BufferedImage> roomImages = new HashMap<>();
	static {
		File dir = new File("Resources/textures/map_levels");
		if(dir.isDirectory()) {
			for (File file1 : dir.listFiles()) {
				if(file1.getPath().contains(".json")){
					try {
						rooms.put(file1.getName().split("\\.")[0], gson.fromJson(new FileReader(file1), RoomData.class));
					}
					catch (IOException e) {e.printStackTrace();}
				}
				else if(file1.getPath().contains(".png")){
					try {
						roomImages.put(file1.getName().split("\\.")[0], ImageIO.read(file1));
					}
					catch (IOException e) {e.printStackTrace();}
				}
			}
		}
	}

	protected static Tuple<RoomData, BufferedImage> getRoom(String key){
		return new Tuple<>(rooms.get(key), roomImages.get(key));
	}

	protected static List<Tuple<RoomData, BufferedImage>> getRoomsWithArg(String... args){
		List<Tuple<RoomData, BufferedImage>> list = new ArrayList<>();
		rooms.forEach((key, roomData) -> {
			for (String arg : args){
				if(!roomData.roomArgs.contains(arg)) return;
			}
			list.add(new Tuple<>(rooms.get(key), roomImages.get(key)));
		});
		return list;
	}

	protected static List<String> getRoomKeysWithArg(String... args){
		List<String> list = new ArrayList<>();
		rooms.forEach((key, roomData) -> {
			for (String arg : args){
				if(!roomData.roomArgs.contains(arg)) return;
			}
			list.add(key);
		});
		return list;
	}

	protected static class RoomData{
		protected int tileSize;
		protected List<AABB> collision;
		protected Vector2f startPos;
		protected AABB exitBox;
		protected List<String> roomArgs;
		protected List<RoomDecorData> decorations;
	}
	protected static class RoomDecorData{
		protected float x,y;
		protected String key;
	}
}
