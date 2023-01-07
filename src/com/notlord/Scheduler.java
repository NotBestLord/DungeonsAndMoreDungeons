package com.notlord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Scheduler {
	private final Map<Integer, Runnable> perTick = new ConcurrentHashMap<>();
	private final Map<Integer, DelayedRunnable> delayed = new ConcurrentHashMap<>();
	private final Map<Integer, RepeatRunnable> repeat = new ConcurrentHashMap<>();
	private final Map<Integer, RepeatDurationRunnable> repeatFor = new ConcurrentHashMap<>();
	private final Map<Integer, RunnableSequence> sequences = new ConcurrentHashMap<>();

	private int id = 0;

	public Scheduler() {
		Thread thread = new Thread(() -> {
			long last = System.nanoTime();
			while (Main.windowOpen) {
				long start = System.nanoTime();
				long passed = start - last;
				last = start;
				double dt = passed / (double) 1000000000;
				perTick.values().forEach(Runnable::run);
				for (Integer key : delayed.keySet()) {
					if (delayed.get(key).nextTick(dt)) delayed.remove(key);
				}
				repeat.values().forEach(r -> r.nextTick(dt));
				for (Integer key : repeatFor.keySet()) {
					if (repeatFor.get(key).nextTick(dt)) repeatFor.remove(key);
				}
				for (Integer key : sequences.keySet()) {
					if (sequences.get(key).nextTick(dt)) sequences.remove(key);
				}
			}
		}, "MAIN_SCHEDULER");
		thread.start();
	}

	public int schedulePerTick(Runnable runnable){
		int id = this.id;
		this.id++;
		perTick.put(id, runnable);
		return id;
	}

	public int scheduleTickDuration(Runnable runnable, float duration){
		int id = this.id;
		this.id++;
		repeatFor.put(id, new RepeatDurationRunnable(runnable, 0, duration));
		return id;
	}

	public int scheduleDelay(Runnable runnable, float delay){
		int id = this.id;
		this.id++;
		delayed.put(id, new DelayedRunnable(runnable, delay));
		return id;
	}

	public int scheduleRepeat(Runnable runnable, float delay){
		int id = this.id;
		this.id++;
		repeat.put(id, new RepeatRunnable(runnable, delay));
		return id;
	}

	public int scheduleRepeatDuration(Runnable runnable, float delay, float duration){
		int id = this.id;
		this.id++;
		repeatFor.put(id, new RepeatDurationRunnable(runnable, delay, duration));
		return id;
	}

	/**
	 *
	 * @param runnableSequence list of delays (float) and Runnables.
	 * @return
	 */
	public int scheduleSequence(Object... runnableSequence){
		int id = this.id;
		this.id++;
		sequences.put(id, new RunnableSequence(runnableSequence));
		return id;
	}

	public void removeSequence(int id){
		sequences.remove(id);
	}

	private static class DelayedRunnable{
		private final Runnable runnable;
		private final float delay;
		private double timePassed = 0;
		public DelayedRunnable(Runnable runnable, float delay) {
			this.runnable = runnable;
			this.delay = delay;
		}

		public boolean nextTick(double dt){
			timePassed += dt;
			if(timePassed >= delay){
				runnable.run();
				return true;
			}
			return false;
		}
	}

	private static class RepeatRunnable{
		private final Runnable runnable;
		private final float delay;
		private double timePassed = 0;

		public RepeatRunnable(Runnable runnable, float delay) {
			this.runnable = runnable;
			this.delay = delay;
		}

		public void nextTick(double dt){
			timePassed += dt;
			if(timePassed >= delay){
				runnable.run();
				while (delay != 0 && timePassed >= delay){
					timePassed -= delay;
				}
			}
		}
	}

	public static class RepeatDurationRunnable{
		private final Runnable runnable;
		private final float delay, duration;
		private double timePassed = 0, totalTime = 0;
		public RepeatDurationRunnable(Runnable runnable, float delay, float duration) {
			this.runnable = runnable;
			this.delay = delay;
			this.duration = duration;
		}

		public boolean nextTick(double dt){
			timePassed += dt;
			totalTime += dt;
			if(timePassed >= delay){
				runnable.run();
				while (delay != 0 && timePassed >= delay){
					timePassed -= delay;
				}
			}
			return totalTime >= duration;
		}
	}

	public static class RunnableSequence{
		private final List<Object> objects;
		private float timer = 0;

		public RunnableSequence(Object[] objects) {
			this.objects = new ArrayList<>(Arrays.asList(objects));
		}

		public boolean nextTick(double dt){
			if(objects.isEmpty()) return true;
			if(objects.get(0) instanceof Float f){
				timer += dt;
				if(timer >= f){
					objects.remove(0);
					while (timer >= f){
						timer -=f;
					}
				}
			}
			else if(objects.get(0) instanceof Runnable runnable){
				runnable.run();
				objects.remove(0);
			}
			else if(objects.get(0) instanceof RepeatDurationRunnable r && r.nextTick(dt)){
				objects.remove(0);
			}
			return false;
		}

	}
}
