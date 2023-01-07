package com.notlord.game;

import com.notlord.math.AABB;
import com.notlord.math.Vector2f;

public interface IPosition {
	int getIntY();
	int getIntX();
	float getX();
	float getY();
	void setX(float x);
	void setY(float y);
	AABB getBoundingBox();
	Vector2f getPosition();
}
