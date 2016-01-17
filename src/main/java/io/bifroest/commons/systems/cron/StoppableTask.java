package io.bifroest.commons.systems.cron;

public interface StoppableTask {
	void stopYourself();
	void join() throws InterruptedException;
}
