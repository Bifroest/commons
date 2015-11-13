package com.goodgame.profiling.commons.collections.versioned_strings;

import java.util.Collection;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import com.goodgame.profiling.commons.model.Metric;

public class MetricMatcher extends ArgumentMatcher<Collection<Metric>> {
    private final String name;
    private final double value;
    private final double precision;

    private MetricMatcher(String name, double value, double precision) {
        this.name = name;
        this.value = value;
        this.precision = precision;
    }

    @Override
    public boolean matches( Object argument ) {
        if ( !(argument instanceof Collection )) {
            return false;
        }

        Collection rawCastedType = (Collection) argument;
        for (Object element : rawCastedType) {
            if ( element instanceof Metric ) {
                Metric m = (Metric) element;

                boolean nameIsEqual = m.name().equals(this.name);
                boolean doubleIsEqual = Math.abs(m.value() - this.value) < this.precision;
                if ( nameIsEqual && doubleIsEqual ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void describeTo( Description d ) {
        d.appendText( "A collection containing a metric with name=" + name + " and value approx.=" + value );
    }
    
    public static MetricMatcher containsMetric(String name, double value) {
        return containsMetric(name, value, 0.001);
    }

    public static MetricMatcher containsMetric(String name, double value, double precision) {
        return new MetricMatcher(name, value, precision);
    }
}
