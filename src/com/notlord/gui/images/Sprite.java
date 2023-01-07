package com.notlord.gui.images;

import com.notlord.gui.rendering.IDraw;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Sprite implements IDraw {
	protected final BufferedImage image;
	private float angle = 0;
	private boolean flipped = false;
	public Sprite(String key) {
		this.image = SpriteUtils.getImage(key);
	}

	public BufferedImage getImage() {
		return image;
	}

	public Sprite(BufferedImage image){
		this.image = image;
	}

	public Sprite(Sprite sprite){
		image = sprite.image;
	}

	public boolean isFlipped() {
		return flipped;
	}

	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	public void draw(Graphics g, int dx, int dy){
		BufferedImage image = SpriteUtils.rotateImage(angle, this.image);
		if(flipped) {
			g.drawImage(SpriteUtils.flipHorizontally(image), dx, dy, null);
		}
		else{
			g.drawImage(image, dx, dy, null);
		}
	}

	public void draw(Graphics g, int dx1, int dy1, int dx2, int dy2){
		BufferedImage image = SpriteUtils.rotateImage(angle, this.image);
		if(flipped) {
			g.drawImage(SpriteUtils.flipHorizontally(image)
					, dx1, dy1, dx2, dy2, 0, 0, image.getWidth(), image.getHeight(), null);
		}
		else{
			g.drawImage(image, dx1, dy1, dx2, dy2, 0, 0, image.getWidth(), image.getHeight(), null);
		}
	}

	public void drawCentered(Graphics g, int dx,int dy){
		BufferedImage image = SpriteUtils.rotateImage(angle, this.image);
		int dx1 = dx - image.getWidth()/2;
		int dy1 = dy - image.getHeight()/2;
		if(flipped) {
			g.drawImage(SpriteUtils.flipHorizontally(image)
					, dx1, dy1, image.getWidth(), image.getHeight(), null);
		}
		else{
			g.drawImage(image, dx1, dy1, image.getWidth(), image.getHeight(), null);
		}
	}


	@Override
	public void setRotation(float angle) {
		this.angle = angle;
	}
}
