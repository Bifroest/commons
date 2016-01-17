package io.bifroest.commons.statistics.push_strategy.plaintext_carbon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;

import org.slf4j.Logger;

import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.model.Metric;
import io.bifroest.commons.statistics.EnvironmentWithStatisticsGatherer;
import io.bifroest.commons.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

public class PlainTextCarbonPushStrategy<E extends EnvironmentWithStatisticsGatherer> extends StatisticsPushStrategyWithTask<E> {
    private static final Logger log = LogService.getLogger(PlainTextCarbonPushStrategy.class);

    private final String carbonHost;
    private final int carbonPort;

    public PlainTextCarbonPushStrategy( String metricPrefix, Duration each, String strategyName, String carbonHost, int carbonPort ) {
        super( metricPrefix, each, strategyName );
        this.carbonHost = carbonHost;
        this.carbonPort = carbonPort;
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) throws IOException {
        log.debug( "Number of metrics: {}", metrics.size() );
        StringBuilder buffer = new StringBuilder();
        for( Metric metric : metrics ) {
            log.trace( metric.toString() );
            String line = metric.name() + " " + metric.value() + " " + metric.timestamp() + "\n";
            buffer.append( line );
        }

        log.debug( "Opening Socket to {}:{}", carbonHost, carbonPort );
        try( Socket carbonSocket = new Socket( carbonHost, carbonPort )) {
            BufferedWriter toCarbon = new BufferedWriter( new OutputStreamWriter( carbonSocket.getOutputStream() ) );
            toCarbon.write( buffer.toString() );
            toCarbon.flush();
        }
        log.debug( "Closed Socket to {}:{}", carbonHost, carbonPort );
    }
}
