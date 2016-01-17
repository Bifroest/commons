package io.bifroest.commons.statistics.push_strategy;

import org.json.JSONObject;

import io.bifroest.commons.boot.interfaces.Environment;

public interface StatisticsPushStrategyFactory<E extends Environment> {
    StatisticsPushStrategy<E> create( JSONObject config );
    String handledType();
}
