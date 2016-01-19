package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;

import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MinAggregationTest {
        @Test
        public void testValueAggregation() {
            assertThat(new MinAggregation(), aggregatesValues(1, 10, 20, 15).into(1));
        }
        
        @Test
        public void testZeroValueAggregation() {
            assertThat(new MinAggregation(), aggregatesValues().into(Double.MAX_VALUE));
        }
        
        @Test
        public void testReset() {
            MinAggregation subject = new MinAggregation();
            
            assertThat(subject, aggregatesValues(1, 20, 3).into(1));
            
            subject.reset();
            
            assertThat(subject, aggregatesValues(3, 4, 5).into(3));
        }

}
