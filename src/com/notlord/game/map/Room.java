package com.notlord.game.map;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.ITick;
import com.notlord.game.ai.EnemyType;
import com.notlord.game.server.Enemy;
import com.notlord.game.server.Player;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Intersections;
import com.notlord.math.Vector2f;
import com.notlord.networking.packets.ExitPercentPacket;
import com.notlord.utils.Tuple;
import com.notlord.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.notlord.Constants.ROOM_SIZE_MUL;

public class Room implements IRender, ITick {
	protected final float tileSize;
	private final Sprite exit_lock;
	public final Sprite sprite;
	private final List<AABB> collisionBoxes = new ArrayList<>();
	private final Vector2f startPos;
	private final AABB exitBox;
	private float exitPercentage = 0;
	private final List<String> roomArgs;
	public final List<RoomDecor> decorations = new ArrayList<>();
	private String key;
	public int enemies = 0;

	private Room(Tuple<MapIndexer.RoomData, BufferedImage> tuple) {
		tileSize = tuple.getV().tileSize;
		Sprite exit_lock = new Sprite("exit_lock");
		BufferedImage image = new BufferedImage((int) (exit_lock.getImage().getWidth() * ROOM_SIZE_MUL),
				(int) (exit_lock.getImage().getHeight() * ROOM_SIZE_MUL), BufferedImage.TYPE_INT_ARGB);
		image.getGraphics().drawImage(exit_lock.getImage().getSubimage(0,0,exit_lock.getImage().getWidth(),
				exit_lock.getImage().getHeight()),0,0,image.getWidth(), image.getHeight(),null);
		this.exit_lock = new Sprite(image);
		image = new BufferedImage((int) (tuple.getK().getWidth() * ROOM_SIZE_MUL),
				(int) (tuple.getK().getHeight() * ROOM_SIZE_MUL), BufferedImage.TYPE_INT_ARGB);
		image.getGraphics().drawImage(tuple.getK().getSubimage(0,0,tuple.getK().getWidth(),
				tuple.getK().getHeight()),0,0,image.getWidth(), image.getHeight(),null);
		sprite = new Sprite(image);
		startPos = new Vector2f(tuple.getV().startPos).mul(tuple.getV().tileSize*ROOM_SIZE_MUL).
				subtract(new Vector2f(sprite.getImage().getWidth() / 2f, sprite.getImage().getHeight() / 2f));
		exitBox = new AABB(tuple.getV().exitBox);
		exitBox.min.mul(tuple.getV().tileSize*ROOM_SIZE_MUL).
				subtract(new Vector2f(sprite.getImage().getWidth() / 2f, sprite.getImage().getHeight() / 2f));
		exitBox.max.mul(tuple.getV().tileSize*ROOM_SIZE_MUL).
				subtract(new Vector2f(sprite.getImage().getWidth() / 2f, sprite.getImage().getHeight() / 2f));
		for (AABB aabb : tuple.getV().collision) {
			AABB box = new AABB(aabb);
			box.min.mul(tuple.getV().tileSize*ROOM_SIZE_MUL).subtract(new Vector2f(sprite.getImage().getWidth() / 2f, sprite.getImage().getHeight() / 2f));
			box.max.mul(tuple.getV().tileSize*ROOM_SIZE_MUL).subtract(new Vector2f(sprite.getImage().getWidth() / 2f, sprite.getImage().getHeight() / 2f));
			collisionBoxes.add(box);
		}
		roomArgs = tuple.getV().roomArgs;
		if(tuple.getV().decorations != null){
			for (MapIndexer.RoomDecorData decor : tuple.getV().decorations) {
				decorations.add(new RoomDecor(decor.key,decor.x,decor.y,ROOM_SIZE_MUL,tileSize,sprite.getImage().getWidth()/2f,sprite.getImage().getHeight()/2f));
			}
		}
	}

	public Room(String key){
		this(MapIndexer.getRoom(key));
		setKey(key);
	}

	public String getKey() {
		return key;
	}

	private void setKey(String key) {
		this.key = key;
	}

	private float timer = 0;
	private float lastP = 0;
	public boolean nextRoom = false;
	@Override
	public void nextTick(float deltaTime) {
		if(Main.serverHandler.server.isRunning()){
			if(enemies != 0){

				return;
			}
			//
			for(Player player : Main.serverHandler.getAllPlayers()){
				if(!Intersections.AABBIntersectsAABB(player.getBoundingBox(), exitBox)){
					if(timer != 0) {
						timer = 0;
						setExitProgress(0);
						Main.serverHandler.server.sendAll(new ExitPercentPacket(0));
					}
					return;
				}
			}
			timer += deltaTime;
			if(timer >= Constants.EXIT_TIMER_SEC){
				nextRoom = true;
				Main.scheduler.scheduleDelay(() -> Main.serverHandler.generateNewDungeon(), 0.1f);
			}
			else{
				float p = timer / Constants.EXIT_TIMER_SEC;
				setExitProgress(p);
				if(p > lastP + 0.1f){
					lastP = p;
					Main.serverHandler.server.sendAll(new ExitPercentPacket(p));
				}
				else if(lastP != 0 && p == 0){
					lastP = p;
					Main.serverHandler.server.sendAll(new ExitPercentPacket(0));
				}
			}
		}
	}

	public boolean isOutOfBounds(AABB other){
		Vector2f p1 = other.min;
		Vector2f p2 = other.max;
		Vector2f p3 = new Vector2f(other.min.x, other.max.y);
		Vector2f p4 = new Vector2f(other.max.x, other.min.y);
		boolean b1=false,b2=false,b3=false,b4=false;
		for (AABB box : collisionBoxes) {
			b1 = b1 || box.contains(p1);
			b2 = b2 || box.contains(p2);
			b3 = b3 || box.contains(p3);
			b4 = b4 || box.contains(p4);
		}
		return !(b1 && b2 && b3 && b4);
	}

	public Vector2f getStartPos() {
		return startPos;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		sprite.draw(g,offsetX-sprite.getImage().getWidth()/2, offsetY-sprite.getImage().getHeight()/2);
		/*for (AABB collisionBox : collisionBoxes) {
			collisionBox.render(g,offsetX,offsetY);
		}
		exitBox.render(g,offsetX,offsetY);*/
		if(exitPercentage != 0){
			g.setColor(Color.cyan);
			g.fillRect(exitBox.min.getIntX()+offsetX,exitBox.min.getIntY()+(exitBox.getHeightInt() / 2)+offsetY,
					(int) (exitBox.getWidthInt() * exitPercentage), exitBox.getHeightInt() / 2);
		}
		if(enemies != 0){
			g.drawImage(exit_lock.getImage(),exitBox.min.getIntX()+offsetX, exitBox.min.getIntY() - exit_lock.getImage().getHeight() + offsetY, null);
		}
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.BACKGROUND;
	}

	public void setExitProgress(float percent){
		exitPercentage = percent;
	}

	/**
	 * selects room based on avg player level.
	 */
	public static Room selectRoom(List<Player> players){
		String key = Utils.getRandom(MapIndexer.getRoomKeysWithArg());
		Room room = new Room(key);
		room.setKey(key);
		for (String arg : room.roomArgs) {
			switch (arg.split(";")[0].split("_")[0]){
				case "enr" -> {
					String[] args1 = arg.split(";");
					String top = args1[0];
					String[] args2 = top.split("_");
					int enemyNum = 0;
					if (args2.length == 2) {
						String range = args2[1];
						String[] rangeArgs = range.split("-");
						if (rangeArgs.length != 2) break;
						try {
							int min = Integer.parseInt(rangeArgs[0]);
							int max = Integer.parseInt(rangeArgs[1]);
							double add = Math.random() * (max - min);
							enemyNum = (int) (min + Math.floor(add));
						} catch (Exception ignored) {
							break;
						}
					}
					if (enemyNum == 0) break;
					Map<EnemyType, Float> chances = new HashMap<>();
					for (int i = 1; i < args1.length; i++) {
						String enemySpawnData = args1[i];
						String[] spawnArgs = enemySpawnData.split(":");
						try {
							chances.put(EnemyType.valueOf(spawnArgs[0]), Float.parseFloat(spawnArgs[1]));
						} catch (Exception ignored) {
							System.out.println("Failed to generate Spawn Args [" + enemySpawnData + "]");
						}
					}
					//
					chances = chances.entrySet().stream().sorted(Map.Entry.comparingByValue()).
							collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
					room.enemies = enemyNum;
					for (int i = 0; i < enemyNum; i++) {
						float c = (float) Math.random();
						for (Map.Entry<EnemyType, Float> e : chances.entrySet()) {
							if (c <= e.getValue()) {
								AABB aabb = Utils.getRandom(room.collisionBoxes);
								Vector2f pos = aabb.getRandomPoint();
								Main.serverHandler.addEntity(new Enemy(pos.x, pos.y, 3, e.getKey().name(), aabb));
								break;
							}
						}
					}
				}
				case "trade" -> {

				}
			}
		}
		return room;
	}

}
