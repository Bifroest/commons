package com.goodgame.profiling.commons.statistics;

import org.slf4j.Logger;

import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusRegistrationPoint;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.util.stopwatch.AsyncClock;

public class SimpleProgramStateTracker {
    private static final Logger log = LogService.getLogger(
            SimpleProgramStateTracker.class);

    // most of these are quasi-final, i.e. they are set once
    private final String contextIdentifier;

    private String[] substorageName = new String[0];

    private SimpleProgramStateTracker( String contextIdentifier ) {
        this.contextIdentifier = contextIdentifier;
    }

    public static SimpleProgramStateTracker forContext( String contextIdentifier ) {
        return new SimpleProgramStateTracker( contextIdentifier );
    }

    public SimpleProgramStateTracker storingIn( String... substorageName ) {
        this.substorageName = substorageName;
        return this;
    }

    public void build() {
        AsyncClock clock = new AsyncClock();
        DirectProgramStateTracker tracker = DirectProgramStateTracker.newTracker(clock);
        EventBusRegistrationPoint registrationPoint = EventBusManager.createRegistrationPoint();

        registrationPoint.sub( ProgramStateChanged.class, e -> {
            log.trace( "Received ProgramStateChanged in context {} to {}.", contextIdentifier, e.contextIdentifier() );
            if ( e.contextIdentifier().equals( this.contextIdentifier ) ) {
                clock.setInstant( e.when() );
                if ( e.nextState().isPresent() ) {
                    tracker.startState( e.threadId(), e.nextState().get() );
                } else {
                    tracker.stopState( e.threadId() );
                }
            }
        }).sub( WriteToStorageEvent.class, e -> {
            clock.setInstant( e.when() );
            
            MetricStorage destination = e.storageToWriteTo();

            for ( String subStorageNamePart : substorageName ) {
                destination = destination.getSubStorageCalled( subStorageNamePart );
            }

            tracker.storeIn( destination );
        });
    }
}
