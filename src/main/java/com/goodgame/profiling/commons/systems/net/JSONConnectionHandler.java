package com.goodgame.profiling.commons.systems.net;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.statistics.ProgramStateChanged;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.systems.net.jsonserver.CommandGroup;
import com.goodgame.profiling.commons.systems.net.socket.ReadWriteableClientSocket;
import com.goodgame.profiling.commons.util.Either;

public final class JSONConnectionHandler<E extends Environment> implements IncomingConnectionHandler {
    private static final Logger log = LogService.getLogger(JSONConnectionHandler.class);

    private final E environment;
    private final Instant createdAt = Clock.systemUTC().instant();
    private final ReadWriteableClientSocket socket;
    private final String name;
    private final CommandGroup<E> commands;

    public JSONConnectionHandler( E environment, ReadWriteableClientSocket socket, String name, CommandGroup<E> commands ) {
        this.environment = Objects.requireNonNull( environment );
        this.socket = Objects.requireNonNull( socket );
        this.name = Objects.requireNonNull( name );
        this.commands = Objects.requireNonNull( commands );
    }

    @Override
    public void run() {
        if ( log.isTraceEnabled() ) {
            log.trace( EventBusManager.eventBusToString() );
        }
        // deliberately create events in the past to get thread IDs right
        EventBusManager.fire( new ProgramStateChanged( ServerThread.CLIENT_TIMING + name, Optional.of( "queued" ), Thread.currentThread().getId(), createdAt ) );
        try {
            if ( socket.isClosed() ) {
                log.debug( "Socket already closed" );
                return;
            }
            getJSONRequestFromClient()
                .then( req -> {
                    ProgramStateChanged.fireContextChangeToState( ServerThread.CLIENT_TIMING + name, "execution" );
                    return commands.executeJSON( req, environment );
                })
                .consume( this::writeToClient, this::writeToClient );
        } catch ( Exception e ) {
            log.warn( "Unexpected Exception while serving " + socket.getTypeWithInfo(), e );
        } finally {
            ProgramStateChanged.fireContextStopped( ServerThread.CLIENT_TIMING + name );
            try {
                socket.close();
            } catch ( IOException e ) {
                log.warn( "Exception while closing socket:", e );
            }
        }
    }

    private Either<JSONObject, JSONObject> getJSONRequestFromClient() {
        ProgramStateChanged.fireContextChangeToState( ServerThread.CLIENT_TIMING + name, "read-input-json" );
        try {
            JSONObject request = new JSONObject( new JSONTokener( socket.getInputStream() ) );
            return Either.ofGoodValue( request );
        } catch ( JSONException | UnsupportedOperationException | IOException e ) {
            log.warn( "Unable to understand client request from " + socket.getTypeWithInfo(), e );

            JSONObject answer = new JSONObject();
            answer.put( "error-source", "request-error" );
            answer.put( "error", e.getClass().getSimpleName() );
            answer.put( "message", e.getMessage() );
            return Either.ofBadValue( answer );
        }
    }

    private void writeToClient( JSONObject jo ) {
        ProgramStateChanged.fireContextChangeToState( ServerThread.CLIENT_TIMING + name, "writing-json" );
        try ( BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) ) ) {
            if( log.isTraceEnabled() ) {
                log.trace( "Sending to client: " + jo.toString() );
            }
            jo.write( writer );
            writer.write( '\n' );
        } catch ( IOException e ) {
            log.warn( "Cannot send result to client", e);
        }
    }
}
