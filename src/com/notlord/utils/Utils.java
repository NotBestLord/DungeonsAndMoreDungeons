package com.notlord.utils;

import java.util.List;
import java.util.Random;

public class Utils {

	@SafeVarargs
	public static <T> T getRandom(T... values){
		return values[new Random().nextInt(values.length)];
	}

	public static <T> T getRandom(List<T> values){
		return values.get(new Random().nextInt(values.size()));
	}

	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void sleep(long millis, int nanos){
		try {
			Thread.sleep(millis, nanos);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
