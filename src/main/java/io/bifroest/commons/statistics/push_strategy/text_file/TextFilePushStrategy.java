package io.bifroest.commons.statistics.push_strategy.text_file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;

import org.slf4j.Logger;

import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.model.Metric;
import io.bifroest.commons.output.AtomicWriteFileOutputStream;
import io.bifroest.commons.statistics.EnvironmentWithStatisticsGatherer;
import io.bifroest.commons.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

public class TextFilePushStrategy<E extends EnvironmentWithStatisticsGatherer> extends StatisticsPushStrategyWithTask<E> {
    private static final Logger log = LogService.getLogger(TextFilePushStrategy.class);

    private final Path path;

    public TextFilePushStrategy( String metricPrefix, Duration each, String strategyName, Path path ) {
        super( metricPrefix, each, strategyName );
        this.path = path;
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) throws IOException {
        try ( AtomicWriteFileOutputStream output = new AtomicWriteFileOutputStream( path ) ) {
            try( BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( output ) )) {
                log.debug( "Writing metrics to {}", path );
                for( Metric metric : metrics ) {
                    writer.write( metric.name() );
                    writer.write( " " );
                    writer.write( String.valueOf( metric.value() ) );
                    writer.write( " " );
                    writer.write( String.valueOf( metric.timestamp() ) );
                    writer.newLine();
                }
                writer.flush();
            }
        }
    }
}
