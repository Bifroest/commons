package io.bifroest.commons.systems.net.jsonserver.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.statistics.storage.JSONMetricStorage;
import io.bifroest.commons.systems.SystemIdentifiers;
import io.bifroest.commons.systems.net.jsonserver.Command;
import io.bifroest.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import io.bifroest.commons.systems.statistics.push_strategy.StatisticsPushStrategies;

@MetaInfServices
public class GetStatisticsCommand< E extends EnvironmentWithStatisticsGatherer > implements Command<E> {
    private static final Logger log = LogService.getLogger(GetStatisticsCommand.class);

    @Override
    public String getJSONCommand() {
        return "get-statistics";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        JSONMetricStorage storage = new JSONMetricStorage();

        StatisticsPushStrategies.collectMetrics( storage );

        try {
            storage.finishStoringTheMetrics();
        } catch( IOException e ) {
            log.warn( "IOException while finishing storing metrics in JSONMetricStorage - this shouldn't happen!", e );
        }

        return storage.storageAsJSON();
    }

    @Override
    public void addRequirements( Collection<String> dependencies ) {
        dependencies.add( SystemIdentifiers.STATISTICS );
    }
}
