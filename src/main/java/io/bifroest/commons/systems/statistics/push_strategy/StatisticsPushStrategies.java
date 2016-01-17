package io.bifroest.commons.systems.statistics.push_strategy;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;

import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.statistics.WriteToStorageEvent;
import io.bifroest.commons.statistics.eventbus.EventBusManager;
import io.bifroest.commons.statistics.storage.MetricStorage;
import io.bifroest.commons.statistics.storage.TrieMetricStorage;
import io.bifroest.commons.systems.cron.TaskRunner;
import io.bifroest.commons.systems.cron.TaskRunner.TaskID;
import io.bifroest.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import io.bifroest.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

public final class StatisticsPushStrategies {
    private static final Logger log = LogService.getLogger(StatisticsPushStrategies.class);
    private static final Clock clock = Clock.systemUTC();

    private StatisticsPushStrategies() {
        // utility class
    }

    public static < E extends EnvironmentWithStatisticsGatherer >
            void enablePeriodicPush( E environment, StatisticsPushStrategyWithTask<E> strategy, String nameOfSubMetric, Duration each, String strategyName )
    {
        TaskID taskId = TaskRunner.runRepeated( ( ) -> writeMetrics( strategy, nameOfSubMetric, each.dividedBy( 2 ), each.dividedBy( 2 )  ),
                                                "StatisticsWriter-" + strategyName,
                                                Duration.ZERO, each,
                                                false );
        strategy.setTaskId( taskId );
    }

    public static void collectMetrics( MetricStorage storage ) {
        EventBusManager.synchronousFire( new WriteToStorageEvent( Clock.systemUTC(), storage) );
    }

    public static < E extends EnvironmentWithStatisticsGatherer >
            void writeMetrics( StatisticsPushStrategy<E> strategy, String nameOfSubMetric, Duration collectLimit, Duration pushLimit ) {
        Instant start = clock.instant();
        final TrieMetricStorage storage = new TrieMetricStorage();
        final MetricStorage storageForGatherers = storage.getSubStorageCalled( nameOfSubMetric );
        collectMetrics( storageForGatherers );
        Instant afterCollect = clock.instant();

        try {
            strategy.pushAll( storage.getAll() );
        } catch( IOException e ) {
            log.warn( "Exception while writing Metrics", e );
        }
        Instant complete = clock.instant();

        Duration collectDuration = Duration.between( start, afterCollect );
        Duration pushDuration = Duration.between( afterCollect, complete );
        Duration totalDuration = Duration.between( start, complete );

        log.debug( "Timings | Collect: {} | Push: {} | Total: {}", collectDuration, pushDuration, totalDuration );
        if ( collectDuration.compareTo( collectLimit ) >= 0 ) {
            log.warn( "Collecting metrics took {} > limit = {}", collectDuration, collectLimit );
        }
        if ( pushDuration.compareTo( pushLimit ) >= 0 ) {
            log.warn( "Pushing metrics took {} > limit = {}", pushDuration, pushLimit );
        }
    }

}
