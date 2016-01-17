package io.bifroest.commons.cron;

public interface StoppableTask {
	void stopYourself();
	void join() throws InterruptedException;
}
