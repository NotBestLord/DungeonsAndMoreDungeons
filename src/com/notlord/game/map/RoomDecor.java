package com.notlord.game.map;

import com.notlord.game.IPosition;
import com.notlord.gui.images.Sprite;
import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;

import java.awt.*;

public class RoomDecor implements IRender, IPosition {
	private final Sprite sprite;
	private final float x,y;
	private final int dx,dy;
	private final float s1,s2;
	public RoomDecor(String sprite, float x, float y, float spriteSize, float tileSize, float ox, float oy){
		this.sprite = new Sprite(sprite);
		this.x = ((x * tileSize * spriteSize) - ox);
		this.y = ((y * tileSize * spriteSize) - oy);
		this.dx = (int) ((((x * tileSize) - this.sprite.getImage().getWidth()/2f) * spriteSize) - ox);
		this.dy = (int) ((((y * tileSize) - this.sprite.getImage().getHeight()) * spriteSize) - oy);
		this.s1 = spriteSize;
		this.s2 = tileSize;
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.drawImage(sprite.getImage(), offsetX + dx, offsetY + dy,(int) (sprite.getImage().getWidth() * s1), (int) (sprite.getImage().getHeight() * s1), null);
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.NORMAL;
	}

	@Override
	public int getIntY() {
		return (int) y;
	}

	@Override
	public int getIntX() {
		return (int) x;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setX(float x) {

	}

	@Override
	public void setY(float y) {

	}

	@Override
	public AABB getBoundingBox() {
		return null;
	}

	@Override
	public Vector2f getPosition() {
		return new Vector2f(x,y);
	}
}
