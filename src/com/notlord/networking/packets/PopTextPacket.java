package com.notlord.networking.packets;

import com.notlord.gui.interaction.PopText;
import com.notlord.math.Vector2f;

import java.awt.*;

public class PopTextPacket {

	private final String fontName;
	private final int fontStyle;
	private final int fontSize;
	private final int textColor;
	private final String string;
	private final float x,y;
	private final boolean centered;
	private final Vector2f velocity;
	private final float duration;
	private final String anchor;

	public PopTextPacket(PopText popText, String anchor) {
		fontName = popText.getFont().getFontName();
		fontStyle = popText.getFont().getStyle();
		fontSize = popText.getFont().getSize();
		textColor = popText.getTextColor().getRGB();
		string = popText.string;
		x = popText.getX();
		y = popText.getY();
		centered = popText.isCentered();
		velocity = popText.getVelocity();
		duration = popText.getDuration();
		this.anchor = anchor;
	}

	public PopText getPopText() {
		return new PopText(new Font(fontName,fontStyle,fontSize), new Color(textColor), string,x,y,velocity,duration, centered,anchor);
	}
}
