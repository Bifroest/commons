package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesNoValues;
import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SumAggregationTest {
    @Test
    public void testNoValueAggregation() {
        assertThat(new SumAggregation(), aggregatesNoValues().into(0));
    }
    
    @Test
    public void testConsumeValue() {
        assertThat(new SumAggregation(), aggregatesValues(1, 2, 3).into(6));
    }
    
    @Test
    public void testReset() {
        ValueAggregation subject = new SumAggregation();
        
        assertThat(subject, aggregatesValues(1, 2, 3, 4).into(10));
        subject.reset();
        assertThat(subject, aggregatesValues(5, 5, 5).into(15));
    }
}
