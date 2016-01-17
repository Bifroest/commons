package io.bifroest.commons.systems.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import io.bifroest.commons.boot.interfaces.Subsystem;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.statistics.eventbus.EventBus;
import io.bifroest.commons.statistics.eventbus.EventBusManager;
import io.bifroest.commons.statistics.eventbus.disruptor.DisruptorEventBus;
import io.bifroest.commons.statistics.gathering.CompositeStatisticGatherer;
import io.bifroest.commons.statistics.gathering.StatisticGatherer;
import io.bifroest.commons.systems.SystemIdentifiers;
import io.bifroest.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import io.bifroest.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import io.bifroest.commons.systems.statistics.push_strategy.StatisticsPushStrategyCreator;

@MetaInfServices
public class StatisticsSystem<E extends EnvironmentWithJSONConfiguration & EnvironmentWithMutableStatisticsGatherer> implements
        Subsystem<E> {

    private static final Logger log = LogService.getLogger( StatisticsSystem.class );

    private JSONObject config;

    private StatisticsPushStrategy<E> pushStrategy;

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.STATISTICS;
    }

    @Override
    public void configure( JSONObject configuration ) {
        config = configuration.getJSONObject( "statistics" );
        JSONObject pushConfig = config.getJSONObject( "metric-push" );
        pushStrategy = new StatisticsPushStrategyCreator<E>().create( pushConfig );
        log.info( "Created Push Strategy {}", pushStrategy );
    }

    public void addRequirements( List<String> destination ) {
        pushStrategy.addRequirements( destination );
    }

    @Override
    public void boot( E environment ) {
        JSONObject eventBusConfig = config.getJSONObject( "eventbus" );
        EventBus bus = new DisruptorEventBus( eventBusConfig.getInt( "handler-count" ),
                eventBusConfig.getInt( "size-exponent" ) );
        // must be done before StatisticGatherer.init
        EventBusManager.setEventBus( bus );
        EventBusManager.actuallyRegisterHandlers();

        StatisticGatherer composite = new CompositeStatisticGatherer();
        composite.init();
        environment.setStatisticGatherer( composite );

        pushStrategy.boot( environment );
    }

    @Override
    public void shutdown( E environment ) {
        try {
            pushStrategy.close();
        } catch( IOException e ) {
            log.warn( "Exception while closing push strategy", e );
        }
        pushStrategy = null;

        try {
            EventBusManager.shutdownEventBus();
        } catch( InterruptedException e ) {
            log.warn( "Interrupted while shutting down eventBus", e );
        }
    }

    @Override
    public Collection<String> getRequiredSystems() {
        List<String> requirements = new ArrayList<String>(10);
        pushStrategy.addRequirements( requirements );
        return requirements;
    }
}
