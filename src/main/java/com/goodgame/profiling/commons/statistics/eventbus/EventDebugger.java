package com.goodgame.profiling.commons.statistics.eventbus;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public class EventDebugger implements StatisticGatherer {

    private static final Logger log = LogService.getLogger(EventDebugger.class);
    private static final Marker EVENT_MARKER = MarkerFactory.getMarker( "EVENT_MARKER" );

    @Override
    public void init() {
        // The event debugger puts some stress on our EventBus thread.
        // Only register it, if event debugging is actually enabled.
        // Because of that, to use event debugging, you have to restart the service.
        if ( log.isTraceEnabled() ) {
            log.warn( "Registering event debugger" );
            EventBusManager.createRegistrationPoint().sub( Object.class, e -> {
                log.trace( EVENT_MARKER, e.toString() );
            } );
        } else {
            log.info( "NOT registering event debugger" );
        }
    }
}
