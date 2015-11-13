package com.goodgame.profiling.commons.util.panic;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;

import com.goodgame.profiling.commons.logging.LogService;

public class PanicAnnouncement implements PanicAction {
    private static final Logger log = LogService.getLogger(PanicAnnouncement.class);

    @Override
    public void execute( Instant now ) {
        log.warn( "ServerPanic Triggered" );
    }

    @Override
    public Duration getCooldown() {
        return Duration.ofMinutes( 1 );
    }
}
