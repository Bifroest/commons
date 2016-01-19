package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesNoValues;
import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;

import io.bifroest.commons.statistics.aggregation.WindowAverageAggregation;
import io.bifroest.commons.statistics.aggregation.ValueAggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class WindowAverageTest {

        @Test
        public void testLessValuesThanWindowSize() {
            ValueAggregation subject = new WindowAverageAggregation(3);
            assertThat(subject, aggregatesValues(16, 8).into(12));
        }
        
        @Test
        public void testMoreValuesThanWindowSize() {
            ValueAggregation subject = new WindowAverageAggregation(3);
            assertThat(subject, aggregatesValues(4, 10, 10, 10).into(10));
        }
        
        @Test
        public void testNoAggregation() {
            ValueAggregation subject = new WindowAverageAggregation(3);
            assertThat(subject, aggregatesNoValues().into(0));
        }
        
        @Test
        public void testReset() {
            ValueAggregation subject = new WindowAverageAggregation(3);
            assertThat(subject, aggregatesValues(10, 10).into(10));
            subject.reset();
            assertThat(subject, aggregatesValues(4, 4, 4).into(4));
        }
}
