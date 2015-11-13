package com.goodgame.profiling.commons.statistics;


import static com.goodgame.profiling.commons.collections.versioned_strings.MetricMatcher.containsMetric;
import static org.junit.Assert.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import com.goodgame.profiling.commons.statistics.storage.TrieMetricStorage;
import com.goodgame.profiling.commons.util.stopwatch.AsyncClock;

public class DirectProgramStateTrackerTest {
    private static final long ERNIE = 1;
    private static final long BERT = 2;

    private AsyncClock trackerTime;
    private DirectProgramStateTracker subject;
    
    @Before
    public void setup() {
        trackerTime = new AsyncClock();
        subject = DirectProgramStateTracker.newTracker( trackerTime );
    }

    @Test
    public void singleThreadedStartStop() {
        startStateAt(ERNIE, 0, "cooking" );
        stopStateAt( ERNIE, 1 );
        assertThatTimeSpentInState( "cooking", 1 );
    }

    @Test
    public void multiThreadedStartStop() {
        startStateAt(ERNIE, 0, "cooking");
        startStateAt(BERT , 5, "cooking");
        stopStateAt( ERNIE, 10);
        stopStateAt( BERT , 15);
        assertThatTimeSpentInState( "cooking", 20 );
    }

    @Test
    public void multiThreadedStateTransitions() {
        startStateAt(ERNIE, 0, "shopping" );
        startStateAt(BERT , 1, "cooking" );
        startStateAt(BERT , 2, "eating" );
        startStateAt(ERNIE, 3, "eating" );
        stopStateAt( ERNIE, 4 );
        stopStateAt( BERT,  5 );

        assertThatTimeSpentInState( "shopping", 3 );
        assertThatTimeSpentInState( "cooking", 1 );
        assertThatTimeSpentInState( "eating", 4 );
    }

    private void assertThatTimeSpentInState( String state, int expectedSeconds ) {
        TrieMetricStorage storage = new TrieMetricStorage();
        subject.storeIn( storage );

        assertThat( storage.getAll(), containsMetric( state, expectedSeconds * 1e9 ) );
    }

    private void startStateAt( long threadId, long second, String state) {
        trackerTime.setInstant( Instant.ofEpochSecond( second ) );
        subject.startState( threadId, state );
    }

    private void stopStateAt( long threadId, long second ) {
        trackerTime.setInstant( Instant.ofEpochSecond( second ) );
        subject.stopState( threadId );
    }
}
