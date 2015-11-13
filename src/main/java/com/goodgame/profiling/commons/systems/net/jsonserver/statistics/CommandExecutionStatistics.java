package com.goodgame.profiling.commons.systems.net.jsonserver.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.duration.PartitionedDurationStatistics;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;

@MetaInfServices
public class CommandExecutionStatistics implements StatisticGatherer {
    private static final Logger log = LogService.getLogger(CommandExecutionStatistics.class);

    private final PartitionedDurationStatistics stats = new PartitionedDurationStatistics( 1000, 10, 5 );

    // ThreadId -> Command -> Timestamp
    private final Map<Long, Map<String, Instant>> startTimes = new HashMap<>();

    @Override
    public void init() {
        EventBusManager.createRegistrationPoint().sub( CommandStartedEvent.class, event -> {
            if ( !startTimes.containsKey( event.threadId() ) ) {
                startTimes.put( event.threadId(), new HashMap<>() );
            }
            startTimes.get( event.threadId() ).put( event.command(), event.when() );
            log.debug( "Executing " + event.command() );
        } ).sub( CommandFinishedEvent.class, event -> {
            stats.handleCall( startTimes.get( event.threadId() ).get( event.command() ), event.command(), event.when() );
            log.debug( "Done " + event.command() );
        } ).sub( WriteToStorageEvent.class, event -> {
            MetricStorage substorage = event.storageToWriteTo().getSubStorageCalled( "commandExecution" );
            stats.writeInto( substorage );
        } );
    }
}
