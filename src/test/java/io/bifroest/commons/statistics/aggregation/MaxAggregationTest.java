package io.bifroest.commons.statistics.aggregation;

import io.bifroest.commons.statistics.aggregation.MaxAggregation;
import io.bifroest.commons.statistics.aggregation.ValueAggregation;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MaxAggregationTest {

	private static final double DELTA = 0.1;

	@Test
	public void checkValueAggregation() {
		ValueAggregation subject = new MaxAggregation();

		assertEquals( 0, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 10 );
		assertEquals( 10, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 20 );
		assertEquals( 20, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 15 );
		assertEquals( 20, subject.getAggregatedValue(), DELTA );
	}

}
