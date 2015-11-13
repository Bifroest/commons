package com.goodgame.profiling.commons.statistics.eventbus;

public interface EventBus {
    void fire( final Object event );
    void synchronousFire( final Object event );
    EventBusRegistrationPoint createRegistrationPoint();

    void describeSubscribersInLog( String outerIndent );

    default void describeSubscribersInLog() {
        describeSubscribersInLog( "" );
    }
    
    // FIXME: Remove
    void shutdown() throws InterruptedException;
}
