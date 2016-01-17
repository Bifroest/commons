package io.bifroest.commons.statistics.push_strategy;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.model.Metric;

public interface StatisticsPushStrategy<E extends Environment> extends Closeable {
    
    void pushAll( Collection<Metric> metrics ) throws IOException;
    
    void boot( E environment );
    
    void addRequirements( List<String> destination );
}
