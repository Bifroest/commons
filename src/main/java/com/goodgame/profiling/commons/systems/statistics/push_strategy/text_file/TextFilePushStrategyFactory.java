package com.goodgame.profiling.commons.systems.statistics.push_strategy.text_file;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.units.parse.DurationParser;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyFactory;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

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
