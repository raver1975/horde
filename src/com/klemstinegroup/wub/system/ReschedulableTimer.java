package com.klemstinegroup.wub.system;

import java.util.Timer;
import java.util.TimerTask;

class ReschedulableTimer extends Timer {
	private Runnable task;
	private TimerTask timerTask;

	public void schedule(Runnable runnable, long delay) {
		task = runnable;
		timerTask = new TimerTask() {
			public void run() {
				task.run();
			}
		};
		this.schedule(timerTask, delay);
	}

	public void reschedule(long delay) {
		timerTask.cancel();
		timerTask = new TimerTask() {
			public void run() {
				task.run();
			}
		};
		this.schedule(timerTask, delay);
	}
}