package com.goodgame.profiling.commons.statistics.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventBusManager {
    private static final Logger log = LogManager.getLogger();
    
    private static EventBus eventBus = null;
    private static boolean delayedRegsFlushed = false;
    
    private static final List<DelayedRegistrationPoint> registrationPoints = new ArrayList<>();
    
    @Deprecated
    // Don't use this method.
    // Use the EventBusManager's methods instead.
    public static EventBus getEventBus() {
        ensureEventBusExists();
        return eventBus;
    }

    public static enum EventBusForce { VROOM, NO_FORCE };

    public static void setEventBus(EventBus newEventBus) {
        setEventBus(newEventBus, EventBusForce.NO_FORCE);
    }
    
    public static void setEventBus(EventBus newEventBus, EventBusForce force) {
        if ( force == EventBusForce.NO_FORCE && eventBus != null ) {
            log.error("Overwriting eventbus");
            log.error("Dropping the following subscribers" );
            eventBus.describeSubscribersInLog( "\t" );
            throw new IllegalStateException("Something was already registered on the eventbus");
        }

        if ( force == EventBusForce.VROOM ) {
            // in case of force, reset everything
            registrationPoints.clear();
            delayedRegsFlushed = false;
        }
        
        eventBus = Objects.requireNonNull( newEventBus );
    }

    private static void ensureEventBusExists() {
        if ( eventBus == null  ) {
            eventBus = new EventBusImpl();
        }
    }

    public static void fire( final Object event ) {
        ensureEventBusExists();
        eventBus.fire( event );
    }
    public static void synchronousFire( final Object event ) {
        ensureEventBusExists();
        eventBus.synchronousFire( event );
    }

    public static EventBusRegistrationPoint createRegistrationPoint() {
        if (delayedRegsFlushed) {
            return eventBus.createRegistrationPoint();
        } else {
            DelayedRegistrationPoint pointForLater = new DelayedRegistrationPoint();
            registrationPoints.add(pointForLater);
            return pointForLater;
        }
    }

    public static void shutdownEventBus() throws InterruptedException {
        if ( eventBus != null ) {
            eventBus.shutdown();
        }
    }

    public static String eventBusToString() {
        ensureEventBusExists();
        return eventBus.toString();
    }

    public static void actuallyRegisterHandlers() {
        ensureEventBusExists();
        registrationPoints.forEach(drp -> drp.addSubscribersTo(eventBus));
        registrationPoints.clear();
        delayedRegsFlushed = true;
    }
}
