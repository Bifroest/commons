package io.bifroest.commons.cron.observer;

public interface RepeatingTaskObserver {
	void threadStarted(long startTimestampInMillis);
	void threadStopped(long startTimestampInMillis);
}
