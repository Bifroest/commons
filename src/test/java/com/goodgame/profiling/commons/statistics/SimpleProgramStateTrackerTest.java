package com.goodgame.profiling.commons.statistics;

import static com.goodgame.profiling.commons.statistics.EventAssert.whenISetup;
import static com.goodgame.profiling.commons.statistics.eventbus.EventBusManager.EventBusForce.VROOM;

import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusImpl;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;

public class SimpleProgramStateTrackerTest {
    @Before
    public void resetEventBus() {
        EventBusManager.setEventBus(new EventBusImpl(), VROOM);
    }

    @Test
    public void testSingleThreadSingleStateDurationTracking() {
        String contextIdentifier = "someContext";
        whenISetup(() -> SimpleProgramStateTracker.forContext( contextIdentifier )
                         .storingIn( "s1.s2" )
                         .build() )
            .andIFire(new ProgramStateChanged( contextIdentifier,
                                                       Optional.of("fixing-atlas"),
                                                       1,
                                                       Instant.ofEpochMilli( 0 )))
            .andIFire(new ProgramStateChanged( contextIdentifier,
                      Optional.empty(),
                      1,
                      Instant.ofEpochMilli( 1000 )))
            .andICollectMetricsAt( Instant.ofEpochMilli( 2000 ))
            .thenIExpectTheMetric("s1.s2.fixing-atlas", 1e9);
    }

    @Test
    public void testContinuousStateisTracked() {
        String contextIdentifier = "someContext";
        whenISetup(() -> SimpleProgramStateTracker.forContext(contextIdentifier)
                                                  .storingIn("s1.s2")
                                                  .build())
            .andIFire(new ProgramStateChanged( contextIdentifier,
                                               Optional.of("testing"),
                                               1,
                                               Instant.ofEpochMilli( 0 )))
            .andICollectMetricsAt( Instant.ofEpochMilli( 1000 ) )
            .thenIExpectTheMetric( "s1.s2.testing", 1e9 );
    }
    
    @Test
    public void testTwoThreadsSingleStateDurationTracking() {
        String contextIdentifier = "someContext";
        whenISetup(() -> SimpleProgramStateTracker.forContext( contextIdentifier )
                                                  .storingIn( "s1.s2" )
                                                  .build())
            .andIFire(new ProgramStateChanged( contextIdentifier,
                                               Optional.of("fixing-atlas"),
                                               1,
                                               Instant.ofEpochMilli( 0 )))
            .andIFire(new ProgramStateChanged( contextIdentifier,
                                               Optional.of("fixing-atlas"),
                                               2,
                                               Instant.ofEpochMilli( 0 )))
            .andIFire(new ProgramStateChanged( contextIdentifier,
                                               Optional.empty(),
                                               1,
                                               Instant.ofEpochMilli( 1000 )))
            .andIFire(new ProgramStateChanged( contextIdentifier,
                                               Optional.empty(),
                                               2,
                                               Instant.ofEpochMilli( 1000 )))
            .andICollectMetricsAt( Instant.ofEpochMilli( 2000 ))
            .thenIExpectTheMetric( "s1.s2.fixing-atlas",  2 * 1e9 );
    }
}
