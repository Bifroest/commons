package io.bifroest.commons.statistics.storage;

import org.json.JSONObject;

public interface ReadableMetricStorage extends MetricStorage {

    JSONObject storageAsJSON();

}
