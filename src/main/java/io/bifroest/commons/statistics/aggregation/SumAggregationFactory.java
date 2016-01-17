package io.bifroest.commons.statistics.aggregation;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public final class SumAggregationFactory implements ValueAggregationFactory {
    @Override
    public String getFunctionName() {
            return "sum";
    }

    @Override
    public ValueAggregation createAggregation() {
        return new SumAggregation();
    }
}
