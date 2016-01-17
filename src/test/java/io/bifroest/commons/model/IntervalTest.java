package io.bifroest.commons.model;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Ignore;
import org.junit.Test;

public final class IntervalTest {
    @Test
    public void testConstructorReordersArgumentsSoStartIsSmallTimestamp() {
        long smallerStart = 5;
        long biggerEnd = 28;

        Interval interval = new Interval(biggerEnd, smallerStart);

        assertThat(interval.start(), is(smallerStart));
    }

    @Test
    public void testConstructorReordersArgumentsSoStartIsBigTimestamp() {
        long smallerStart = 5;
        long biggerEnd = 28;

        Interval interval = new Interval(biggerEnd, smallerStart);

        assertThat(interval.end(), is(biggerEnd));
    }

    @Test
    public void testContainsStartIsInclusive() {
        long smallerStart = 5;
        long biggerEnd = 28;

        Interval interval = new Interval(smallerStart, biggerEnd);

        assertThat(interval.contains(smallerStart), is(true));
    }

    @Test
    public void testContainsEndIsExclusive() {
        long smallerStart = 5;
        long biggerEnd = 28;

        Interval interval = new Interval(smallerStart, biggerEnd);

        assertThat(interval.contains(biggerEnd), is(false));
    }

    @Test
    public void testIntersectIsFalseForDisjointIntervals() {
        long startOfFirst = 5;
        long endOfFirst = 10;

        long startOfSecond = endOfFirst; // endOfFirst is NOT part of the first interval
        long endOfSecond = 15;

        Interval earlierInterval = new Interval(startOfFirst, endOfFirst);
        Interval laterInterval = new Interval(startOfSecond, endOfSecond);

        assertThat(earlierInterval.intersects(laterInterval), is(false));
    }

    @Test
    public void testIntersectIsFalseForFarDisjointIntervals() {
        long startOfFirst = 5;
        long endOfFirst = 6;

        long startOfSecond = 0;
        long endOfSecond = 1;

        Interval firstInterval = new Interval(startOfFirst, endOfFirst);
        Interval secondInterval = new Interval(startOfSecond, endOfSecond);

        assertThat(firstInterval.intersects(secondInterval), is(false));
    }

    @Test
    public void testIntersectIsTrueIfLaterIntervalContainsEndOfSmallerInterval() {
        long startOfEarlier = 5;
        long endOfEarlier = 10;

        long startOfLaterBeforeEndOfEarlier = 7;
        long endOfLaterAfterEndOfEarlier = 12;

        Interval earlierInterval = new Interval(startOfEarlier, endOfEarlier);
        Interval laterInterval = new Interval(startOfLaterBeforeEndOfEarlier, endOfLaterAfterEndOfEarlier);

        assertThat(earlierInterval.intersects(laterInterval), is(true));
        assertThat(laterInterval.intersects(earlierInterval), is(true));
    }

    @Test
    public void testHashCodeDependsOnStartAndEnd() {
        long someStart = 10;
        long someEnd = 100;

        Interval firstInterval = new Interval(someStart, someEnd);
        Interval secondInterval = new Interval(someStart, someEnd);

        assertThat(firstInterval.hashCode() == secondInterval.hashCode(), is(true));
    }

    @Test
    public void testEqualsHonorsIdentity() {
        long someStart = 10;
        long someEnd = 1000;

        Interval interval = new Interval(someStart, someEnd);

        assertThat(interval.equals(interval), is(true));
    }

    @Test
    public void testEqualsHandlesNull() {
        long someStart = 10;
        long someEnd = 1000;

        Interval interval = new Interval(someStart, someEnd);

        assertThat(interval.equals(null), is(false));
    }

    @Test
    public void testEqualsHandlesNonsenseCompares() {
        long someStart = 10;
        long someEnd = 1000;

        Interval interval = new Interval(someStart, someEnd);

        assertThat(interval.equals("nonsense"), is(false));
    }

    @Test
    public void testEqualsComparesStart() {
        long someStart = 10;
        long someDifferentStart = 11;
        long someEqualEnd = 1000;

        Interval first = new Interval(someStart, someEqualEnd);
        Interval second = new Interval(someDifferentStart, someEqualEnd);

        assertThat(first.equals(second), is(false));
        assertThat(second.equals(first), is(false));
    }

    @Test
    public void testEqualsComparesEnd() {
        long someEqualStart = 10;

        long someEnd = 100;
        long someDifferentEnd = 200;

        Interval first = new Interval(someEqualStart, someEnd);
        Interval second = new Interval(someEqualStart, someDifferentEnd);

        assertThat(first.equals(second), is(false));
        assertThat(second.equals(first), is(false));
    }

    @Test
    public void testEqualsConsidersSameObjectsEqual() {
        long someStart = 10;
        long someEnd = 100;

        Interval first = new Interval(someStart, someEnd);
        Interval second = new Interval(someStart, someEnd);

        assertThat(first.equals(second), is(true));
        assertThat(second.equals(first), is(true));
    }
}
