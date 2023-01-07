package com.notlord.gui.images;

import com.notlord.gui.rendering.IDraw;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class AnimatedSprite implements IDraw {
	private BufferedImage image;
	private int rows;
	private int columns;
	protected final int[] frames;
	protected final float nextFrame;
	private boolean flipped = false;
	private boolean looping = false;
	private float angle = 0;
	protected final HashMap<AnimState, int[]> animationParts;
	/**
	 * @param frames frames used in animation
	 * @param nextFrameTime time between frame change
	 */
	public AnimatedSprite(int[] frames, float nextFrameTime,boolean looping,HashMap<AnimState, int[]> animationParts) {
		this.frames = frames;
		this.nextFrame = nextFrameTime;
		this.currentFrame = 0;
		this.looping = looping;
		this.animationParts = animationParts;
	}

	protected AnimatedSprite(BufferedImage image, int rows, int columns, int[] frames, float nextFrame,boolean looping,HashMap<AnimState, int[]> animationParts) {
		this.image = image;
		this.rows = rows;
		this.columns = columns;
		this.frames = frames;
		this.nextFrame = nextFrame;
		this.looping = looping;
		this.animationParts = animationParts;
	}

	private float timer = 0;
	private int currentFrame;
	public void reset(){
		currentFrame = 0;
		flipped = false;
	}

	public void nextTick(float dt){
		if(frames.length == 1 || (!looping && currentFrame == frames.length-1)) return;
		timer += dt;
		while (timer >= nextFrame){
			currentFrame++;
			if(currentFrame == frames.length){
				currentFrame = 0;
			}
			timer -= nextFrame;
		}
	}
	public void setAtlas(BufferedImage image, int rows, int columns){
		this.image = image;
		this.rows = rows;
		this.columns = columns;
	}
	public void nextFrame(){
		nextTick(nextFrame);
	}

	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	public boolean isFlipped() {
		return flipped;
	}

	public boolean isLooping() {
		return looping;
	}

	public boolean ended(){
		return currentFrame == frames.length-1;
	}

	@Override
	public void setRotation(float angle) {
		this.angle = angle;
	}

	@Override
	public void draw(Graphics g, int dx, int dy) {
		int sx1 = (frames[currentFrame] % columns) * (image.getWidth()/columns);
		int sy1 = (frames[currentFrame] % rows) * (image.getHeight()/rows);
		BufferedImage frameImage = image.getSubimage(sx1,sy1,image.getWidth()/columns,image.getHeight()/rows);
		BufferedImage image = SpriteUtils.rotateImage(angle, frameImage);
		if(flipped){
			g.drawImage(SpriteUtils.flipHorizontally(image),
					dx, dy, image.getWidth()/columns, image.getHeight()/rows, null);
		}
		else{
			g.drawImage(image, dx, dy, image.getWidth()/columns, image.getHeight()/rows, null);
		}
	}

	@Override
	public void draw(Graphics g, int dx1, int dy1, int dx2, int dy2) {
		int sx1 = (frames[currentFrame] % columns) * (image.getWidth() / columns);
		int sy1 = (frames[currentFrame] / columns) * (image.getHeight() / rows);
		BufferedImage frameImage = image.getSubimage(sx1,sy1,image.getWidth()/columns,image.getHeight()/rows);
		BufferedImage image = SpriteUtils.rotateImage(angle, frameImage);
		if(flipped){
			g.drawImage(SpriteUtils.flipHorizontally(image),
					dx1, dy1, dx2, dy2, 0, 0, frameImage.getWidth(), frameImage.getHeight(), null);
		}
		else {
			g.drawImage(image,
					dx1, dy1, dx2, dy2, 0, 0, frameImage.getWidth(), frameImage.getHeight(), null);
		}
	}

	public void drawCentered(Graphics g, int dx, int dy) {
		int sx1 = (frames[currentFrame] % columns) * (image.getWidth() / columns);
		int sy1 = (frames[currentFrame] / columns) * (image.getHeight() / rows);
		BufferedImage frameImage = image.getSubimage(sx1,sy1,image.getWidth()/columns,image.getHeight()/rows);
		Sprite sprite = new Sprite(frameImage);
		sprite.setRotation(angle);
		sprite.setFlipped(flipped);
		sprite.drawCentered(g,dx,dy);
	}

	public int getFrameWidth(){
		return image.getWidth()/columns;
	}

	public int getFrameHeight(){
		return image.getHeight()/rows;
	}

	public boolean hasParts(){
		return animationParts != null;
	}

	public long getLengthInMillis(){
		return (long) (frames.length * nextFrame * 1000);
	}

	public float getLengthInSeconds(){
		return frames.length * nextFrame;
	}

	public int getFrameLengthInMillis(){
		return (int) (nextFrame * 1000);
	}

	public long getStateLengthInMillis(AnimState state){
		return (long) (animationParts.get(state).length * 1000 * nextFrame);
	}

	public float getStateLengthInSeconds(AnimState state){
		return animationParts.get(state).length * nextFrame;
	}

	public float getFrameLengthInSeconds(){
		return nextFrame;
	}

	public AnimatedSpriteSave getSave(){
		return new AnimatedSpriteSave(frames,nextFrame,looping,animationParts);
	}
	public static class AnimatedSpriteSave{
		private final int[] frames;
		private final float nextFrameTime;
		private boolean looping;
		private HashMap<AnimState, int[]> animationParts;
		public AnimatedSpriteSave(int[] frames, float nextFrameTime, boolean looping,HashMap<AnimState, int[]> animationParts) {
			this.frames = frames;
			this.nextFrameTime = nextFrameTime;
			this.looping = looping;
			this.animationParts = animationParts;
		}

		public AnimatedSprite get(AnimatedSpriteHandler handler){
			AnimatedSprite sprite = new AnimatedSprite(frames,nextFrameTime,looping,animationParts);
			sprite.setAtlas(handler.atlas, handler.rows, handler.columns);
			return sprite;
		}
	}
	public enum AnimState {
		START,MIDDLE,END
	}
}
