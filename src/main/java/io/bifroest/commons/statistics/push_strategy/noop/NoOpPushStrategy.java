package io.bifroest.commons.statistics.push_strategy.noop;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.model.Metric;
import io.bifroest.commons.statistics.push_strategy.StatisticsPushStrategy;
import io.bifroest.commons.statistics.push_strategy.StatisticsPushStrategyFactory;

@MetaInfServices(StatisticsPushStrategyFactory.class)
public class NoOpPushStrategy<E extends Environment> implements StatisticsPushStrategy<E>, StatisticsPushStrategyFactory<E> {
    private static final Logger log = LogService.getLogger(NoOpPushStrategy.class);

    @Override
    public StatisticsPushStrategy<E> create( JSONObject config ) {
        return this;
    }

    @Override
    public String handledType() {
        return "no-op";
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) {
        log.debug( "Dropped metrics: {}", metrics );
    }

    @Override
    public void close() throws IOException {
        // Don't do anything
    }

    @Override
    public void boot(E environment) {

    }

    @Override
    public void addRequirements(List<String> destination) {

    }
}
