package io.bifroest.commons.statistics.push_strategy.plaintext_carbon;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import io.bifroest.commons.statistics.units.parse.DurationParser;
import io.bifroest.commons.statistics.EnvironmentWithStatisticsGatherer;
import io.bifroest.commons.statistics.push_strategy.StatisticsPushStrategy;
import io.bifroest.commons.statistics.push_strategy.StatisticsPushStrategyFactory;
import io.bifroest.commons.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

@MetaInfServices
public class PlainTextCarbonPushStrategyFactory<E extends EnvironmentWithStatisticsGatherer>
        implements StatisticsPushStrategyFactory<E> {

    @Override
    public StatisticsPushStrategy<E> create( JSONObject config ) {
        StatisticsPushStrategyWithTask<E> strategy = new PlainTextCarbonPushStrategy<E>(
                config.getString( "base" ),
                ( new DurationParser() ).parse( config.getString( "each" ) ),
                config.getString( "type" ),
                config.getString( "host" ),
                config.getInt( "port" ) );
        return strategy;
    }

    @Override
    public String handledType() {
        return "plain-text-carbon";
    }
}
