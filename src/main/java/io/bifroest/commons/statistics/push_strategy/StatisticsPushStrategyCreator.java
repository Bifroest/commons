package io.bifroest.commons.statistics.push_strategy;

import java.util.ServiceLoader;

import org.json.JSONObject;

import io.bifroest.commons.boot.interfaces.Environment;

public class StatisticsPushStrategyCreator<E extends Environment> {
    @SuppressWarnings( "unchecked" )
    public StatisticsPushStrategy<E> create( JSONObject config ) {
        for ( StatisticsPushStrategyFactory<E> factory : ServiceLoader.load( StatisticsPushStrategyFactory.class ) ) {
            if( factory.handledType().equals( config.getString( "type" ) ) ) {
                return factory.create( config );
            }
        }
        
        throw new IllegalStateException( "No matching StatisticsPushStrategy found!" );
    }
}
