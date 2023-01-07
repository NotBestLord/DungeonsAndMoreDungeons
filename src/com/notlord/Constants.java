package com.notlord;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Constants {
	public static final String TITLE = "Labyrinth Of Cards";
	public static final List<Character> TEXT_BOX_KEYS = new ArrayList<Character>();
	static{
		for(char c : "abcdefghijklmnopqrstuvwxyz0123456789_+-=/|<>,.;:!@#$%^&*(){}[]`~?/ ".toCharArray()){
			TEXT_BOX_KEYS.add(c);
		}
	}

	public static final double NANOSECOND = 1000000000;

	public static final Font DEFAULT_FONT = new Font("Arial",Font.PLAIN,16);
	public static final Canvas DEBUG_CANVAS = new Canvas();

	public static double PANEL_FPS = 60;
	public static final float SERVER_BUFFER_PLAYER_DELAY = 1f;
	public static final float CLIENT_PLAYER_POSITION_ERROR = 7.5f;

	public static final float AI_MIN_DIST = 10;

	public static final float PLAYER_BASE_EXP_REQ = 10f;

	public static final float EXIT_TIMER_SEC = 1.5f;
	public static final float ROOM_SIZE_MUL = 4f;

	public static final int ABILITY_TREE_TILE_SIZE = 32;
	public static final int ABILITY_TREE_SIZE_MUL = 1;
	public static final int ABILITY_TREE_VIS_HEIGHT = 352;

	public static String hostAbilityTreeType = "mage";
	public static String joinAbilityTreeType = "mage";
}
