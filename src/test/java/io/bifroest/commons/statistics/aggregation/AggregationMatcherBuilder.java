package io.bifroest.commons.statistics.aggregation;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class AggregationMatcherBuilder {
    private final double[] inputValues;
    
    private AggregationMatcherBuilder(double[] inputValues) {
        this.inputValues = inputValues;
    }
    
    public static AggregationMatcherBuilder aggregatesValues(double... values) {
        return new AggregationMatcherBuilder(values);
    }
    
    public Matcher<ValueAggregation> into(double expectedValue) {
        return new AggregationHamcrestMatcher(inputValues, expectedValue );
    }
    
    private static class AggregationHamcrestMatcher extends TypeSafeDiagnosingMatcher<ValueAggregation> {
        private static final double DELTA = 0.1;
        
        private final double[] inputValues;
        private final double expectedValue;
        
        private AggregationHamcrestMatcher(double[] inputValues, double expectedValue) {
            this.inputValues = inputValues;
            this.expectedValue = expectedValue;
        }
        
        @Override
        protected boolean matchesSafely( ValueAggregation t, Description mismatchDescription ) {
            aggregateValues(t);
            double aggregatedValue = t.getAggregatedValue();
            mismatchDescription.appendText("was ").appendValue(aggregatedValue);
            return Math.abs(aggregatedValue - expectedValue) < DELTA;
        }

        private void aggregateValues(ValueAggregation subject) {
            for (double inputValue : inputValues) {
                subject.consumeValue( inputValue );
            }
        }
        
        @Override
        public void describeTo( Description matcherDescription ) {
            matcherDescription.appendText("calling aggregate with ").appendValue( inputValues ).appendText( " should output " ).appendValue( expectedValue );
        }
    }
}
