package io.bifroest.commons.statistics.units.parse;

import java.time.Duration;

import io.bifroest.commons.statistics.units.SI_PREFIX;
import io.bifroest.commons.statistics.units.TIME_UNIT;

public class DurationParser {
    private TimeUnitParser internalParser;

    public DurationParser() {
        this.internalParser = new TimeUnitParser( SI_PREFIX.NANO, TIME_UNIT.SECOND );
    }

    public Duration parse( String string ) {
        long parseResultInNanos = internalParser.parse( string ).longValue();
        return Duration.ofNanos( parseResultInNanos );
    }
}
