package com.notlord.math;

public class Intersections {
	public static boolean AABBIntersectsAABB(float ax1, float ay1, float ax2, float ay2, float bx1, float by1, float bx2, float by2){
		/*
			[ax1, ay1]
						[bx1,by1]
								[ax2,ay2]           [bx2,by2]
		 */
		return bx1 < ax2 && bx2 > ax1 && by1 < ay2 && by2 > ay1;
	}

	public static boolean AABBIntersectsAABB(AABB aabb1, AABB aabb2){
		return aabb2.min.x < aabb1.max.x && aabb2.max.x > aabb1.min.x &&
				aabb2.min.y < aabb1.max.y && aabb2.max.y > aabb1.min.y;
	}

	private static Vector2f ClipLine(int d, AABB aabb, Vector2f lineStart, Vector2f lineEnd, Float f_low, Float  f_high) {
		float f_dim_low, f_dim_high;

		f_dim_low = (aabb.min.get(d) - lineStart.get(d))/(lineEnd.get(d) - lineStart.get(d));
		f_dim_high = (aabb.max.get(d) - lineStart.get(d))/(lineEnd.get(d) - lineStart.get(d));

		// Make sure low is less than high
		if (f_dim_high < f_dim_low) {
			float t = f_dim_high;
			f_dim_high = f_dim_low;
			f_dim_low = t;
		}

		if (f_dim_high < f_low)
			return null;
		if (f_dim_low > f_high)
			return null;

		f_low = Math.max(f_dim_low, f_low);
		f_high = Math.min(f_dim_high, f_high);

		if (f_low > f_high)
			return null;

		return new Vector2f(f_low,f_high);
	}


	public static boolean AABBIntersectsLine(AABB aabb1, Vector2f line1, Vector2f line2){
		float low = 0f;
		float high = 1f;
		Vector2f res;
		if((res = ClipLine(0,aabb1,line1, line2,low,high)) == null)
			return false;
		low = res.x;
		high = res.y;
		return ClipLine(1, aabb1, line1, line2, low, high) != null;
	}
}
