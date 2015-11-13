package com.goodgame.profiling.commons.statistics.gathering;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;

import com.goodgame.profiling.commons.logging.LogService;

public class CompositeStatisticGatherer implements StatisticGatherer {
    private static final Logger log = LogService.getLogger(CompositeStatisticGatherer.class);

    private final List<StatisticGatherer> gatherers;
    
    public CompositeStatisticGatherer() {
        gatherers = new ArrayList<>();
        for ( StatisticGatherer sg : ServiceLoader.load( StatisticGatherer.class ) ) {
            log.info( "StatisticGatherer loaded: " + sg.toString() );
            gatherers.add( sg );
        }
    }

    @Override
    public void init() {
        for ( StatisticGatherer sg : gatherers ) {
            sg.init();
        }
    }
}
