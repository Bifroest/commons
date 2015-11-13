package com.goodgame.profiling.commons.statistics.percentile;

import java.util.Arrays;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;

public class PercentileTracker {
    public static void make( String identifier, int windowSize, String metricPrefix, double...percentages ) {
        final Percentiles percentiles = new Percentiles( windowSize );
        final String[] metricNames = Arrays.stream( percentages ).mapToObj( percentage -> String.format( "%07d", (int)(percentage*1000000) ) ).toArray( String[]::new );

        EventBusManager.createRegistrationPoint().sub( PercentileEvent.class, e -> {
            if ( ! e.getType().equals( identifier ) ) {
                return;
            }

            percentiles.add( e.getValue() );
        }).sub( WriteToStorageEvent.class, e -> {
            MetricStorage storage = e.storageToWriteTo().getSubStorageCalled( metricPrefix );
            double[] p = percentiles.getPercentiles( percentages );
            for ( int i = 0; i < percentages.length; i++ ) {
                storage.store( metricNames[i], p[i] );
            };
        });
    }
}
