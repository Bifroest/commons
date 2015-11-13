package com.goodgame.profiling.commons.systems.common;

import java.nio.file.Path;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.EnvironmentWithInit;
import com.goodgame.profiling.commons.boot.InitD;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithMutableJSONConfiguration;
import com.goodgame.profiling.commons.systems.configuration.JSONConfigurationLoader;
import com.goodgame.profiling.commons.systems.net.wamp.EnvironmentWithMutableWampClients;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithMutableStatisticsGatherer;
import java.util.HashMap;
import java.util.Map;
import ws.wamp.jawampa.WampClient;

public abstract class AbstractCommonEnvironment implements EnvironmentWithConfigPath, EnvironmentWithMutableJSONConfiguration,
        EnvironmentWithMutableStatisticsGatherer, EnvironmentWithMutableWampClients, EnvironmentWithInit {

    private final Path configPath;
    private InitD init;
    private JSONObject configuration;
    private JSONConfigurationLoader configLoader;
    private StatisticGatherer statisticGatherer;
    private Map<String, WampClient> wampClients = new HashMap<String, WampClient>();
    
    public AbstractCommonEnvironment( Path configPath, InitD init ) {
        this.configPath = configPath;
        this.init = init;
    }

    @Override
    public Path getConfigPath() {
        return configPath;
    }

    @Override
    public InitD initD() {
        return init;
    }

    public void setInitD( InitD initd ) {
        this.init = initd;
    }

    @Override
    public JSONObject getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration( JSONObject object ) {
        this.configuration = object;
    }

    @Override
    public JSONConfigurationLoader getConfigurationLoader() {
        return configLoader;
    }

    @Override
    public void setConfigurationLoader( JSONConfigurationLoader loader ) {
        this.configLoader = loader;
    }

    @Override
    public StatisticGatherer statisticGatherer() {
        return statisticGatherer;
    }

    @Override
    public void setStatisticGatherer( StatisticGatherer statisticGatherer ) {
        this.statisticGatherer = statisticGatherer;
    }
    
    @Override
    public WampClient getWampClientForRealm( String realm ) {
        return wampClients.get( realm );
    }
    
    @Override
    public void addWampClient( String realm, WampClient client ) {
        if ( wampClients.get( realm ) != null ) {
            throw new IllegalStateException( "Attempting to overwrite wamp client for realm " + realm );
        }
        wampClients.put( realm, client );
    }
}
