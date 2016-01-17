package io.bifroest.commons.statistics;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.collections4.map.LazyMap;

import io.bifroest.commons.statistics.storage.MetricStorage;
import io.bifroest.commons.util.stopwatch.StopWatchWithStates;

public final class DirectProgramStateTracker {
    private static final Object DAS_LOCK = new Object();

    private final Map<Long, StopWatchWithStates> watches;
    
    private DirectProgramStateTracker( Clock clock ) {
        watches = LazyMap.lazyMap(new HashMap<Long, StopWatchWithStates>(),
                                  () -> new StopWatchWithStates( clock ) );

    }

    public void startState( String state ) {
        startState( Thread.currentThread().getId(), state );
    }

    public void stopState() {
        stopState( Thread.currentThread().getId() );
    }

    public void startState( long threadId, String state ) {
        synchronized (DAS_LOCK) {
            watches.get( threadId ).startState( state );
        }
    }

    public void stopState( long threadId ) {
        synchronized (DAS_LOCK) {
            watches.get( threadId ).stop();
        }
    }

    public void storeIn( MetricStorage storage ) {
        Map<String, LongAdder> stageRuntimes = LazyMap.lazyMap( new HashMap<>(), () -> new LongAdder() );
        synchronized (DAS_LOCK) {
            watches.forEach( (id, watch) -> {
                watch.consumeStateDurations( (stage, duration) -> {
                    stageRuntimes.get( stage ).add( duration.toNanos() );
                });
            });
        }

        stageRuntimes.forEach( (stage, time) -> {
            storage.store( stage, time.doubleValue() );
        });
    }

    public static DirectProgramStateTracker newTracker() {
        return newTracker( Clock.systemUTC() );
    }

    public static DirectProgramStateTracker newTracker( Clock time ) {
        return new DirectProgramStateTracker( time );
    }
}
