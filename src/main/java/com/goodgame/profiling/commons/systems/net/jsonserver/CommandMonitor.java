package com.goodgame.profiling.commons.systems.net.jsonserver;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.commons.systems.net.jsonserver.statistics.CommandFinishedEvent;
import com.goodgame.profiling.commons.systems.net.jsonserver.statistics.CommandStartedEvent;

public class CommandMonitor implements Runnable {
    private static final Logger log = LogService.getLogger(CommandMonitor.class);
    private static final TimeFormatter formatter = new TimeFormatter( 2, TIME_UNIT.SECOND );

    private final String interfaceName;
    private final long warnLimit;

    // ThreadID -> ( Timestamp, Command )
    private final ConcurrentMap<Long, Pair<Long, String>> startTimes;

    public CommandMonitor( String interfaceName, long warnLimit ) {
        this.interfaceName = interfaceName;
        this.warnLimit = warnLimit;
        this.startTimes = new ConcurrentHashMap<>();
        init();
    }

    private void init() {
        EventBusManager.createRegistrationPoint().sub( CommandStartedEvent.class, event -> {
            log.trace( "CommandStartedEvent in Thread {}", Thread.currentThread().getId() );
            Objects.requireNonNull( event );
            Objects.requireNonNull( event.interfaceName() );
            if ( event.interfaceName().equals( interfaceName ) ) {
                startTimes.put( event.threadId(), new ImmutablePair<>( System.currentTimeMillis() / 1000, event.command() ) );
            }
        } ).sub( CommandFinishedEvent.class, event -> {
            log.trace( "CommandFinished in Thread {}", Thread.currentThread().getId() );
            if ( event.interfaceName().equals( interfaceName ) ) {
                if ( startTimes.containsKey( event.threadId() ) ) {
                    startTimes.remove( event.threadId() );
                } else {
                    log.warn( "startTimes {} doesn't contain entry for {}", startTimes, event);
                }
            }
        } );
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis() / 1000;
        for ( Entry<Long, Pair<Long, String>> entry : startTimes.entrySet() ) {
            long time = now - entry.getValue().getLeft();
            String command = entry.getValue().getRight();
            if ( time > warnLimit ) {
                log.warn( "Command " + command + " on thread " + entry.getKey() + " is now running " + formatter.format( time ) );
            }
        }
    }
}
