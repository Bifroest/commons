package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesNoValues;
import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;

import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TotalAverageTest {
        @Test
        public void testConsumeValue() {
            assertThat(new TotalAverageAggregation(), aggregatesValues(9, 1, 2, 4, 4).into(4));
        }
        
        @Test
        public void testNoValues() {
            assertThat(new TotalAverageAggregation(), aggregatesNoValues().into(0));
        }
        
        @Test
        public void testReset() {
            TotalAverageAggregation subject = new TotalAverageAggregation();
            
            assertThat(subject, aggregatesValues(9, 1, 2).into(4));
            subject.reset();
            assertThat(subject, aggregatesValues(1, 1, 1).into(1));
        }
}