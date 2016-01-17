package io.bifroest.commons.systems.common;

import java.nio.file.Path;

import org.json.JSONObject;

import io.bifroest.commons.boot.EnvironmentWithInit;
import io.bifroest.commons.boot.InitD;
import io.bifroest.commons.statistics.gathering.StatisticGatherer;
import io.bifroest.commons.systems.configuration.EnvironmentWithMutableJSONConfiguration;
import io.bifroest.commons.systems.configuration.JSONConfigurationLoader;
import io.bifroest.commons.systems.statistics.EnvironmentWithMutableStatisticsGatherer;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCommonEnvironment implements EnvironmentWithConfigPath, EnvironmentWithMutableJSONConfiguration,
        EnvironmentWithMutableStatisticsGatherer, EnvironmentWithInit {

    private final Path configPath;
    private InitD init;
    private JSONObject configuration;
    private JSONConfigurationLoader configLoader;
    private StatisticGatherer statisticGatherer;
    
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
}
