package io.bifroest.commons.statistics.push_strategy.text_file;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import io.bifroest.commons.statistics.units.parse.DurationParser;
import io.bifroest.commons.statistics.EnvironmentWithStatisticsGatherer;
import io.bifroest.commons.statistics.push_strategy.StatisticsPushStrategy;
import io.bifroest.commons.statistics.push_strategy.StatisticsPushStrategyFactory;
import io.bifroest.commons.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

@MetaInfServices
public class TextFilePushStrategyFactory<E extends EnvironmentWithStatisticsGatherer>
    implements StatisticsPushStrategyFactory<E> {
    
    @Override
    public String handledType() {
        return "text-file";
    }

    @Override
    public StatisticsPushStrategy<E> create( JSONObject config ) {
        Path path = Paths.get( config.getString( "path" ) );
        StatisticsPushStrategyWithTask<E> strategy =  new TextFilePushStrategy<>(
                config.getString( "base" ),
                ( new DurationParser() ).parse( config.getString( "each" ) ),
                config.getString( "type" ),
                path );
        return strategy;
    }
}
