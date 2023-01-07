package com.notlord.math;

public class Vector2f {
	public float x;
	public float y;

	public Vector2f() {
		x = 0;
		y = 0;
	}

	public Vector2f(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2f(float angle) {
		this.x = (float) Math.cos(Math.toRadians(angle));
		this.y = (float) Math.sin(Math.toRadians(angle));
	}
	public Vector2f(Vector2f vector2f) {
		this.x = vector2f.x;
		this.y = vector2f.y;
	}

	public float distance(Vector2f other){
		float xd = x-other.x;
		float yd = y-other.y;
		return (float) Math.sqrt(xd*xd + yd*yd);
	}

	public int getIntX() {
		return (int) x;
	}

	public int getIntY() {
		return (int) y;
	}

	public float get(int i){
		return i == 0 ? x : y;
	}

	public void set(Vector2f other){
		this.x = other.x;
		this.y = other.y;
	}

	public Vector2f add(Vector2f vector2f){
		x += vector2f.x;
		y += vector2f.y;
		return this;
	}

	public Vector2f mul(float m){
		x *= m;
		y *= m;
		return this;
	}

	public Vector2f subtract(Vector2f vector2f){
		x -= vector2f.x;
		y -= vector2f.y;
		return this;
	}

	public Vector2f normalize(){
		x = x / Math.abs(x);
		y = y / Math.abs(y);
		return this;
	}

	public float angle(Vector2f other){
		float deg = (float) Math.toDegrees(Math.atan2(other.y - y,other.x - x));
		return deg > 0 ? deg : 360 + deg;
	}

	public Vector2f clone(){
		return new Vector2f(x,y);
	}

	@Override
	public String toString() {
		return "Vector2f{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vector2f v && v.x == x && v.y == y;
	}
}
