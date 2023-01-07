package com.notlord.gui.images;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SpriteUtils {
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<String, AnimatedSpriteHandler> animatedSpriteHandlerMap = new HashMap<>();

	static{
		File dir = new File("Resources/textures/animated");
		if(dir.isDirectory()) {
			for (File file1 : dir.listFiles()) {
				if(file1.getPath().contains(".json")){
					try {
						animatedSpriteHandlerMap.put(file1.getName().split("\\.")[0], getAnimatedSpriteHandlerSaveFromJsonFile(file1.getPath()).get());
					}
					catch (IOException e) {e.printStackTrace();}
				}
			}
		}
	}
	private static final Map<String, BufferedImage> spriteImageMap = new HashMap<>();
	static{
		File dir = new File("Resources/textures/sprites");
		if(dir.isDirectory()) {
			for (File file1 : dir.listFiles()) {
				if(file1.getPath().contains(".png")){
					try {
						spriteImageMap.put(file1.getName().split("\\.")[0], ImageIO.read(file1));
					}
					catch (IOException e) {e.printStackTrace();}
				}
			}
		}
		dir = new File("Resources/textures/gui");
		if(dir.isDirectory()) {
			for (File file1 : dir.listFiles()) {
				if(file1.getPath().contains(".png")){
					try {
						spriteImageMap.put("gui_"+file1.getName().split("\\.")[0], ImageIO.read(file1));
					}
					catch (IOException e) {e.printStackTrace();}
				}
			}
		}
	}

	public static BufferedImage rotateImage(float angle, BufferedImage image){
		if(angle == 0) return image;
		double width = image.getWidth(), height = image.getHeight();
		double size = Math.sqrt((width*width)+(height*height));
		BufferedImage image1 = new BufferedImage((int) size,(int) size,BufferedImage.TYPE_INT_ARGB);
		image1.getGraphics().drawImage(image, (image1.getWidth()-image.getWidth())/2, (image1.getHeight()-image.getHeight())/2,null);
		AffineTransform tx = new AffineTransform();
		tx.translate(size / 2, size / 2);
		tx.rotate(Math.toRadians(angle));
		tx.translate(-size / 2, -size / 2);
		return new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR).filter(image1,null);
	}
	public static BufferedImage flipHorizontally(BufferedImage image) {
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gg = newImage.createGraphics();
		gg.drawImage(image, image.getWidth(), 0, -image.getWidth(), image.getHeight(), null);
		gg.dispose();
		return newImage;
	}

	private static AnimatedSpriteHandler.AnimatedSpriteHandlerSave getAnimatedSpriteHandlerSaveFromJsonFile(String path) throws FileNotFoundException {
		return gson.fromJson(new FileReader(path), AnimatedSpriteHandler.AnimatedSpriteHandlerSave.class);
	}

	/**
	 * USED IN DEV TO SAVE ANIMATED_SPRITES.
	 * @param key name of file
	 * @param save data to save
	 */
	private static void setAnimatedSpriteHandlerSaveToJsonFile(String key, AnimatedSpriteHandler.AnimatedSpriteHandlerSave save) {
		File file = new File("Resources/textures/animated/"+key+".json");
		try {
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(gson.toJson(save));
			writer.flush();
			writer.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public static AnimatedSpriteHandler getAnimatedSpriteHandler(String key){
		if(!animatedSpriteHandlerMap.containsKey(key)) return null;
		AnimatedSpriteHandler out = new AnimatedSpriteHandler(animatedSpriteHandlerMap.get(key).atlas,animatedSpriteHandlerMap.get(key).rows,animatedSpriteHandlerMap.get(key).columns);
		animatedSpriteHandlerMap.get(key).sprites.forEach((s, animatedSprite) -> {
			AnimatedSprite sprite = new AnimatedSprite(out.atlas, out.rows, out.columns, animatedSprite.frames,
					animatedSprite.nextFrame,animatedSprite.isLooping(),animatedSprite.animationParts);
			out.sprites.put(s,sprite);
		});
		return out;
	}

	protected static BufferedImage getImage(String key){
		return spriteImageMap.get(key);
	}
}
