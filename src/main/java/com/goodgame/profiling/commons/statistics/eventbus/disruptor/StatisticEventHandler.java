package com.goodgame.profiling.commons.statistics.eventbus.disruptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusRegistrationPoint;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusSubscriber;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.util.stopwatch.StopWatchWithStates;
import com.lmax.disruptor.EventHandler;

public class StatisticEventHandler implements EventHandler<StatisticEventHolder>, EventBusRegistrationPoint {
    private static final Logger log = LogManager.getLogger();

    private final Map<Class<?>, List<EventBusSubscriber<Object>>> subscriberLists = new HashMap<>();

    private final Clock clock = Clock.systemUTC();
    private final StopWatchWithStates stopwatch = new StopWatchWithStates( clock );

    private static final class ClassAdderPair {
        public Class<?> eventClass;
        public LongAdder adder;
    }

    private ArrayList<ClassAdderPair> eventCounts = new ArrayList<>( 20 );

    public StatisticEventHandler( int index ) {
        subscribe( WriteToStorageEvent.class, e -> {
            MetricStorage workStorage = e.storageToWriteTo();
            // EventBus.Handler-2
            //  .Utilization.WriteToStorage.CommandExecutionStatistics.TimeSpent
            //  .EventsConsumed
            workStorage = workStorage.getSubStorageCalled( "EventBus" );
            MetricStorage finalDestination = workStorage.getSubStorageCalled( "Handler-" + index );
            stopwatch.consumeStateDurations( (state, duration) -> {

                MetricStorage workStorage2 = finalDestination.getSubStorageCalled( "Utilization" )
                                                             .getSubStorageCalled( "Events" );
                for ( String s : StringUtils.split( state, '.' ) ) {
                    workStorage2 = workStorage2.getSubStorageCalled( s );
                }
                workStorage2.store( "TimeSpent", duration.toNanos() );
            });

            MetricStorage countStorage = finalDestination.getSubStorageCalled( "EventsComsumed" );
            Iterator<ClassAdderPair> it = eventCounts.iterator();
            while ( it.hasNext() ) {
                ClassAdderPair n = it.next();
                countStorage.store( unlambda( n.eventClass.getSimpleName() ), n.adder.doubleValue() );
            }
        });
        stopwatch.startState( "idle" );
    }

    // TODO: Remove synchronized with bootloader V2
    @Override
    public synchronized void onEvent( StatisticEventHolder eventHolder, long sequence, boolean endOfBatch ) {
        Object event = eventHolder.get();
        incrementRightAdderForEvent( event.getClass() );
        try {
            String eventName = event.getClass().getSimpleName();
            for ( Map.Entry<Class<?>, List<EventBusSubscriber<Object>>> subscriberList : subscriberLists.entrySet() ) {
                Class<?> eventClass = subscriberList.getKey();
                List<EventBusSubscriber<Object>> subscribers = subscriberList.getValue();

                if ( ! eventClass.isAssignableFrom( event.getClass() ) ) continue;

                for ( EventBusSubscriber<Object> subscriber : subscribers ) {
                    if ( subscriber.getClass().getSimpleName().length() == 0 ){
                        log.warn( "What the hell is " + subscriber + " of class " + subscriber.getClass() + "?!" );
                    }
                    String subscriberName = unlambda( subscriber.getClass().getSimpleName() );
                    stopwatch.startState( eventName + "." + subscriberName );
                    try {
                        Instant start = clock.instant();
                        subscriber.onEvent( event );
                        Instant finished = clock.instant();
                        Duration duration = Duration.between( start, finished );
                        if ( duration.compareTo( Duration.ofMillis( 50 ) ) > 0 ) {
                            log.warn( "EventHandler {} needed {} to handle {}", subscriber, duration, eventName );
                        }
                    } catch ( Exception e ) {
                        log.warn( subscriber + " failed", e );
                    }
                    stopwatch.startState( "idle" );
                }
            }
        } finally {
            eventHolder.countDownLatchIfExists();
        }
    }

    private void incrementRightAdderForEvent( Class<?> firedEventClass ) {
        Iterator<ClassAdderPair> it = eventCounts.iterator();
        while ( it.hasNext() ) {
            ClassAdderPair p = it.next();
            if ( p.eventClass == firedEventClass ) { // deliberate identity equals
                p.adder.increment();
                return;
            }
        }

        ClassAdderPair newPair = new ClassAdderPair();
        newPair.eventClass = firedEventClass;
        newPair.adder = new LongAdder();
        newPair.adder.increment();
        eventCounts.add( newPair );
    }

    // TODO: Remove synchronized with bootloader V2
    @SuppressWarnings( "unchecked" )
    @Override
    public synchronized < EVENT_CLASS > void subscribe( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber ) {
        log.debug( "Registering subscriber {} for event {} on handler {}", subscriber, event, this );
        subscriberList( event ).add( (EventBusSubscriber<Object>) subscriber );
    }

    // TODO: Remove synchronized with bootloader V2
    private synchronized List<EventBusSubscriber<Object>> subscriberList( Class<?> c ) {
        if ( subscriberLists.containsKey( c ) ) {
            return subscriberLists.get( c );
        } else {
            List<EventBusSubscriber<Object>> result = new ArrayList<>();
            subscriberLists.put( c, result );
            return result;
        }
    }

    private String unlambda( String simpleNamePotentiallyWithLambda ) {
        if ( simpleNamePotentiallyWithLambda.length() == 0 ) {
            return "Unknown";
        } else {
            return StringUtils.split( simpleNamePotentiallyWithLambda, "$", 2 )[0]; 
        } 
    }

    public void describeSubscribersInLog( String outerIndent ) {
        log.info( outerIndent + "\t - Registered event subscribers:" );
        for ( List<EventBusSubscriber<Object>> subscribers : subscriberLists.values() ) {
            for ( EventBusSubscriber<Object> subscriber : subscribers ) {
                log.info( outerIndent + "\t\t - "  + unlambda( subscriber.getClass().getSimpleName() ) );
            }
        }
    }
}
