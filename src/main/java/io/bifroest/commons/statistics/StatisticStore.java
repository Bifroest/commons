package io.bifroest.commons.statistics;

import org.json.JSONObject;

public interface StatisticStore {

    void storeMetrics();

    JSONObject getMetrics();

}
