package io.bifroest.commons.systems.statistics;

import org.json.JSONObject;

public interface StatisticStore {

    void storeMetrics();

    JSONObject getMetrics();

}
