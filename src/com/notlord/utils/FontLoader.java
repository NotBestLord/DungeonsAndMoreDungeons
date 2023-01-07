package com.notlord.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FontLoader {
	static {
		File dir = new File("Resources/fonts");
		if (dir.isDirectory()) {
			for (File file1 : dir.listFiles()) {
				if (file1.getPath().contains(".ttf")) {
					try {
						GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
						ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, file1));
					} catch (IOException | FontFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
