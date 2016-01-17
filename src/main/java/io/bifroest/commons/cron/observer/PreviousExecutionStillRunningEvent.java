package io.bifroest.commons.cron.observer;

public class PreviousExecutionStillRunningEvent {
	public final String threadName;

	public PreviousExecutionStillRunningEvent(String threadName) {
		this.threadName = threadName;
	}
	
}
