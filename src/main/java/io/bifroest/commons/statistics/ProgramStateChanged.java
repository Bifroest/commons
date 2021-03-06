package io.bifroest.commons.statistics;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import io.bifroest.commons.statistics.commands.EventWithThreadId;
import io.bifroest.commons.statistics.eventbus.EventBusManager;

public final class ProgramStateChanged implements EventWithThreadId, EventWithInstant {
    private final long threadId;
    private final Instant when;
    private final String contextIdentifier;
    private final Optional<String> nextState;

    public ProgramStateChanged( String contextIdentifier, Optional<String> nextState, long threadId, Instant when ) {
        this.contextIdentifier = contextIdentifier;
        this.nextState = nextState;
        this.threadId = threadId;
        this.when = when;
    }

    public static ProgramStateChanged changedContextToState( String contextIdentifier, String nextState ) {
        return new ProgramStateChanged( contextIdentifier, Optional.of( nextState ),
                                        Thread.currentThread().getId(),
                                        Clock.systemUTC().instant() );
    }

    public static void fireContextChangeToState( String contextIdentifier, String nextState ) {
        EventBusManager.fire( ProgramStateChanged.changedContextToState( contextIdentifier, nextState ) );
    }

    public static ProgramStateChanged contextStopped( String contextIdentifier ) {
        return new ProgramStateChanged( contextIdentifier, Optional.empty(),
                                        Thread.currentThread().getId(),
                                        Clock.systemUTC().instant() );
    }

    public static void fireContextStopped( String contextIdentifier ) {
        EventBusManager.fire( ProgramStateChanged.contextStopped( contextIdentifier ) );
    }

    public String contextIdentifier() {
        return contextIdentifier;
    }

    public Optional<String> nextState() {
        return nextState;
    }

    @Override
    public long threadId() {
        return threadId;
    }

    @Override
    public Instant when() {
        return when;
    }

    @Override
    public boolean equals( Object o ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format( "ProgramStateChanged( context=%s, nextState=%s, tid=%d, when=%s)",
                              contextIdentifier, nextState, threadId, when );
    }
}
