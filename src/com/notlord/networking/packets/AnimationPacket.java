package com.notlord.networking.packets;

public class AnimationPacket {
	private String id;
	private final String animation;
	private final int state;
	private final float rotation;

	/**
	 *
	 * @param id entity id
	 * @param animation entity animation, "NULL"=change flip of current animation
	 * @param state 0=not flipped,1=flipped,2=no change
	 */
	public AnimationPacket(String id, String animation, int state) {
		this.id = id;
		this.animation = animation;
		this.state = state;
		this.rotation = 0;
	}
	public AnimationPacket(String id, String animation, int state, float rotation) {
		this.id = id;
		this.animation = animation;
		this.state = state;
		this.rotation = rotation;
	}

	public String getId() {
		return id;
	}

	public String getAnimation() {
		return animation;
	}

	public int getState() {
		return state;
	}

	public float getRotation() {
		return rotation;
	}
}
