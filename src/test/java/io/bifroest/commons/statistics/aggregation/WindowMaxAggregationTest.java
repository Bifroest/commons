package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesNoValues;
import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class WindowMaxAggregationTest {
    @Test
    public void testAggregateNoValues() {
        int anyWindowSize = 10;
        assertThat(new WindowMaxAggregation(anyWindowSize), aggregatesNoValues().into(Double.MIN_VALUE));
    }
    
    @Test
    public void testAggregateLessValuesThanWindowSize() {
        assertThat(new WindowMaxAggregation(3), aggregatesValues(1, 2).into(2));
    }
    
    @Test
    public void testAggregatesMoreValuesThanWindowSize() {
        assertThat(new WindowMaxAggregation(3), aggregatesValues(100, 1, 2, 3).into(3));
    }
    
    @Test
    public void testReset() {
        ValueAggregation subject = new WindowMaxAggregation(3);
        
        assertThat(subject, aggregatesValues(10, 20).into(20));
        subject.reset();
        assertThat(subject, aggregatesValues(1).into(1));
    }
}
