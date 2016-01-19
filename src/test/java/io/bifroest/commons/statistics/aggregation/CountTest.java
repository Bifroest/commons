package io.bifroest.commons.statistics.aggregation;

import static io.bifroest.commons.statistics.aggregation.AggregationMatcherBuilder.aggregatesValues;

import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CountTest {

        @Test
        public void testConsumeValue() {
            assertThat(new CountAggregation(), aggregatesValues(1, 2, 3, 4, 5).into(5));
        }
        
        @Test
        public void testResetResetsProperly() {
            CountAggregation subject = new CountAggregation();
            assertThat(subject, aggregatesValues(1, 2, 3).into(3));
            subject.reset();
            assertThat(subject, aggregatesValues(1, 2, 3, 4, 5).into(5));
        }
}
