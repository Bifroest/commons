package com.goodgame.profiling.commons.statistics.eventbus;

import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.SimpleProgramStateTracker;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

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
