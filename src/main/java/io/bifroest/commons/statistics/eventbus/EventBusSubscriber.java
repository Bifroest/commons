package io.bifroest.commons.statistics.eventbus;

public interface EventBusSubscriber< EVENT_CLASS > {

	void onEvent( EVENT_CLASS event );

}
