package io.bifroest.commons.statistics.eventbus;

/**
 * An EventBusRegistrationPoint is a class where StatisticsGatherer can
 * register event handlers with an event bus.
 * 
 * If subscribe() is called multiple times on the same
 * EventBusRegistrationPoint, the event handlers are guaranteed to run on the
 * same thread.
 */
public interface EventBusRegistrationPoint {
    @Deprecated
    <EVENT_CLASS> void subscribe( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber );

    default <EVENT_CLASS> EventBusRegistrationPoint sub( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber ) {
        subscribe(event, subscriber);
        return this;
    }
}
