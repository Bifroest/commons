package io.bifroest.commons.statistics.units.format;

import io.bifroest.commons.statistics.units.format.DurationFormatter;
import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.Test;

public class DurationFormatterTest {
    @Test
    public void test() {
        DurationFormatter subject = new DurationFormatter();

        assertEquals("1m 5s", subject.format( Duration.ofSeconds( 65 ) ).trim() );
    }
}
