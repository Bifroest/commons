package io.bifroest.commons.statistics.aggregation;

public interface ValueAggregationFactory { 
    String getFunctionName();
    ValueAggregation createAggregation();
}
