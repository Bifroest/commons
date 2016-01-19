package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;

import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MaxAggregationTest {
        @Test
        public void testValueAggregation() {
            assertThat(new MaxAggregation(), aggregatesValues(1, 10, 20, 15).into(20));
        }
        
        @Test
        public void testZeroValueAggregation() {
            assertThat(new MaxAggregation(), aggregatesValues().into(Double.MIN_VALUE));
        }
        
        @Test
        public void testReset() {
            MaxAggregation anyMaxAggregation = new MaxAggregation();
            
            assertThat(anyMaxAggregation, aggregatesValues(1, 20, 3).into(20));
            
            anyMaxAggregation.reset();
            
            assertThat(anyMaxAggregation, aggregatesValues(3, 4, 5).into(5));
        }
}
