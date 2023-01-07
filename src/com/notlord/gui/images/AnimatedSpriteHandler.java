package com.notlord.gui.images;

import com.notlord.game.ITick;
import com.notlord.gui.rendering.IDraw;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AnimatedSpriteHandler implements ITick, IDraw {
	protected final Map<String, AnimatedSprite> sprites = new HashMap<>();
	private String currentKey = "";
	protected final int rows, columns;
	protected final BufferedImage atlas;
	public AnimatedSpriteHandler(String path, int rows, int columns) {
		BufferedImage image1;
		try {
			image1 = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(path)));
		}
		catch (IOException e){
			image1 = null;
			e.printStackTrace();
		}
		this.atlas = image1;
		this.rows = rows;
		this.columns = columns;
	}
	protected AnimatedSpriteHandler(BufferedImage atlas, int rows, int columns){
		this.atlas = atlas;
		this.rows = rows;
		this.columns = columns;
	}

	protected void add(String key, AnimatedSprite sprite){
		sprites.put(key,sprite);
	}

	public void setCurrentAnimation(String key){
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).reset();
		}
		currentKey = key;
	}

	public void setCurrentAnimationWFlip(String key){
		boolean flipped = isCurrentAnimationFlipped();
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).reset();
		}
		currentKey = key;
		setCurrentAnimationFlipped(flipped);
	}

	public void setCurrentAnimationFlipped(boolean flipped){
		if(sprites.get(currentKey) != null) {
			sprites.get(currentKey).setFlipped(flipped);
		}
	}

	public boolean isCurrentAnimationFlipped(){
		if(sprites.get(currentKey) != null) {
			return sprites.get(currentKey).isFlipped();
		}
		return false;
	}

	public String getCurrentAnimation(){
		return currentKey;
	}

	public int getAnimationFrameLengthInMillis(){
		return sprites.get(currentKey) != null ? sprites.get(currentKey).getFrameLengthInMillis() : 0;
	}
	public float getAnimationFrameLengthInSeconds(){
		return sprites.get(currentKey) != null ? sprites.get(currentKey).getFrameLengthInSeconds() : 0;
	}

	public long getAnimationLengthInMillis(){
		return sprites.get(currentKey) != null ? sprites.get(currentKey).getLengthInMillis() : 0;
	}

	public float getAnimationLengthInSeconds(){
		return sprites.get(currentKey) != null ? sprites.get(currentKey).getLengthInSeconds() : 0;

	}

	public long getAnimationStateLengthInMillis(AnimatedSprite.AnimState state){
		return sprites.get(currentKey) != null && sprites.get(currentKey).hasParts() ? sprites.get(currentKey).getStateLengthInMillis(state) : 0;
	}

	public float getAnimationStateLengthInSeconds(AnimatedSprite.AnimState state){
		return sprites.get(currentKey) != null && sprites.get(currentKey).hasParts() ? sprites.get(currentKey).getStateLengthInSeconds(state) : 0;
	}

	@Override
	public void nextTick(float deltaTime) {
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).nextTick(deltaTime);
			if(!sprites.get(currentKey).isLooping() && sprites.get(currentKey).ended()){
				setCurrentAnimationWFlip("");
			}
		}
	}

	@Override
	public void setRotation(float angle) {
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).setRotation(angle);
		}
	}

	@Override
	public void draw(Graphics g, int dx, int dy) {
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).draw(g,dx,dy);
		}
	}

	@Override
	public void draw(Graphics g, int dx1, int dy1, int dx2, int dy2) {
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).draw(g,dx1,dy1,dx2,dy2);
		}
	}

	public void drawCentered(Graphics g, int dx, int dy) {
		if(sprites.get(currentKey) != null){
			sprites.get(currentKey).drawCentered(g,dx,dy);
		}
	}
	public int getFrameWidth(){
		return sprites.get(currentKey).getFrameWidth();
	}
	public int getFrameHeight(){
		return sprites.get(currentKey).getFrameHeight();
	}

	public static class AnimatedSpriteHandlerSave{
		private final Map<String, AnimatedSprite.AnimatedSpriteSave> sprites;
		private final String path;
		private final int rows, columns;

		public AnimatedSpriteHandlerSave(String path, int rows, int columns, Map<String, AnimatedSprite.AnimatedSpriteSave> sprites) {
			this.sprites = sprites;
			this.path = path;
			this.rows = rows;
			this.columns = columns;
		}

		public AnimatedSpriteHandler get(){
			AnimatedSpriteHandler handler = new AnimatedSpriteHandler(path, rows, columns);
			sprites.forEach((s, animatedSpriteSave) -> handler.sprites.put(s, animatedSpriteSave.get(handler)));
			return handler;
		}
	}
}

