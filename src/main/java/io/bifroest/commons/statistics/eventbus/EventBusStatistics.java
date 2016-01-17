package io.bifroest.commons.statistics.eventbus;

import org.kohsuke.MetaInfServices;

import io.bifroest.commons.statistics.SimpleProgramStateTracker;
import io.bifroest.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public final class EventBusStatistics implements StatisticGatherer {
    public static final String UTILIZATION = "commons.statistics.eventbus.utilization";

    @Override
    public void init() {
        SimpleProgramStateTracker.forContext( UTILIZATION )
                                 .storingIn( "EventBus.Utilization" )
                                 .build();
    }
}
