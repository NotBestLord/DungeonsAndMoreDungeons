package com.notlord.math;

import com.notlord.gui.rendering.IRender;
import com.notlord.gui.rendering.RenderPriority;

import java.awt.*;

public class AABB implements IRender {
	public final Vector2f min;
	public final Vector2f max;

	public AABB(float minX, float minY, float maxX, float maxY) {
		if(minX > maxX){
			float x = maxX;
			maxX = minX;
			minX = x;
		}
		if(minY > maxY){
			float y = maxY;
			maxY = minY;
			minY = y;
		}
		min = new Vector2f(minX,minY);
		max = new Vector2f(maxX,maxY);
	}
	public AABB(double minX, double minY, double maxX, double maxY) {
		this((float) minX,(float) minY,(float) maxX,(float) maxY);
	}
	public AABB(Vector2f min, Vector2f max){
		this(min.x, min.y, max.x,max.y);
	}
	public AABB(AABB aabb){
		this(aabb.min.x, aabb.min.y, aabb.max.x,aabb.max.y);
	}

	public AABB moveX(float x){
		min.x += x;
		max.x += x;
		return this;
	}
	public AABB moveY(float y){
		min.y += y;
		max.y +=  y;
		return this;
	}

	public AABB move(Vector2f vector2f){
		moveX(vector2f.x);
		moveY(vector2f.y);
		return this;
	}

	public boolean contains(int x, int y){
		return x >= min.x && x <= max.x && y >= min.y && y <= max.y;
	}
	public boolean contains(Vector2f point){
		return point.x >= min.x && point.x <= max.x && point.y >= min.y && point.y <= max.y;
	}
	public float getWidth(){
		return max.x - min.x;
	}

	public float getHeight(){
		return max.y - min.y;
	}

	public int getWidthInt(){
		return max.getIntX() - min.getIntX();
	}

	public int getHeightInt(){
		return max.getIntY() - min.getIntY();
	}

	public Vector2f getMiddle(){
		return new Vector2f(min.x+((max.x-min.x)/2),min.y+((max.y-min.y)/2));
	}

	public Vector2f getRandomPoint(){
		return new Vector2f((float) (min.x + (Math.random() * getWidth())),
				(float) (min.y + (Math.random() * getHeight())));
	}

	public void balance(){
		if(min.x > max.x){
			float x = max.x;
			max.x = min.x;
			min.x = x;
		}
		if(min.y > max.y){
			float y = max.y;
			max.y = min.y;
			min.y = y;
		}
	}

	@Override
	public String toString() {
		return "AABB{" +
				"min=" + min +
				", max=" + max +
				'}';
	}

	@Override
	public void render(Graphics g, int offsetX, int offsetY) {
		g.setColor(Color.red);
		g.drawRect(min.getIntX()+offsetX, min.getIntY()+offsetY,getWidthInt(),getHeightInt());
	}

	@Override
	public RenderPriority getPriority() {
		return RenderPriority.GUI;
	}
}
