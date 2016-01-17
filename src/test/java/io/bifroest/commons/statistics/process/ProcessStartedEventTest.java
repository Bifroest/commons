package io.bifroest.commons.statistics.process;

import io.bifroest.commons.statistics.process.ProcessStartedEvent;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProcessStartedEventTest {
    private class Dummy extends ProcessStartedEvent{
        public Dummy( long timestamp ) {
            super( timestamp );
        }
    }

    @Test
    public void testBackwardCompatible() {
        ProcessStartedEvent subject = new Dummy(1234567890l);

        assertEquals(1234567890l, subject.timestamp());
    }
}
