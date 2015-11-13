package com.goodgame.profiling.commons.systems.statistics.push_strategy.composite;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;

public class CompositePushStrategy<E extends Environment>
        implements StatisticsPushStrategy<E> {
    private static final Logger log = LogService.getLogger(CompositePushStrategy.class);

    private final Collection<StatisticsPushStrategy<E>> inners;

    public CompositePushStrategy(Collection<StatisticsPushStrategy<E>> inners) {
        this.inners = inners;
    }

    @Override
    public void pushAll(Collection<Metric> metrics) throws IOException {
        for (StatisticsPushStrategy<E> inner : inners) {
            try {
                inner.pushAll(metrics);
            } catch (Exception e) {
                log.warn("Exception while pushing metrics", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (StatisticsPushStrategy<E> inner : inners) {
            try {
                inner.close();
            } catch (Exception e) {
                log.warn("Exception while closing inner StatisticsPushStrategy {}", inner, e);
            }
        }
    }

    @Override
    public void boot(E environment) {
        inners.stream().forEach((inner) -> {
            inner.boot(environment);
        });
    }

    @Override
    public void addRequirements(List<String> destination) {
        for ( StatisticsPushStrategy<E> inner : inners ) {
            inner.addRequirements( destination );
        }
    }

    @Override
    public String toString() {
        return "CompositePushStrategy [inners=" + inners + "]";
    }
}
