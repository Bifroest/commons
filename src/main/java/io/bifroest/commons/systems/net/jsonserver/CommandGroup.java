package io.bifroest.commons.systems.net.jsonserver;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.statistics.eventbus.EventBusManager;
import io.bifroest.commons.systems.net.jsonserver.commands.GetCommandsCommand;
import io.bifroest.commons.systems.net.jsonserver.statistics.CommandFinishedEvent;
import io.bifroest.commons.systems.net.jsonserver.statistics.CommandStartedEvent;
import io.bifroest.commons.util.Either;

public final class CommandGroup<E extends Environment> {
    private static final Logger log = LogService.getLogger(CommandGroup.class);

    private static final String JSON_COMMAND_KEY = "command";

    private final String interfaceName;

    private final Map<String, Command<E>> nameToCommand;

    private final GetCommandsCommand<E> getCommands;

    public CommandGroup( String interfaceName ) {
        this.interfaceName = Objects.requireNonNull( interfaceName );
        this.nameToCommand = new HashMap<>();
        this.getCommands = new GetCommandsCommand<>();
        this.add( this.getCommands );
    }

    public void add( Command<E> command ) {
        this.nameToCommand.put( command.getJSONCommand().toLowerCase(), command );
        this.getCommands.add( command );
    }

    public Either<JSONObject, JSONObject> executeJSON( JSONObject input, E environment ) {
        log.trace( "Request from client: " + input.toString() );
        if ( !input.has( JSON_COMMAND_KEY ) ) {
            log.warn( "Cannot find command key in " + input.toString() );
            throw new IllegalArgumentException( "No command key in JSON request" );
        }

        String commandFromJSON = input.getString( JSON_COMMAND_KEY );
        Clock clock = Clock.systemUTC();
        EventBusManager.fire( new CommandStartedEvent( clock, commandFromJSON, interfaceName, Thread.currentThread().getId() ) );
        Command<E> c = nameToCommand.get( commandFromJSON.toLowerCase() );
        if ( c == null ) {
            log.warn( "Unsupported command '" + commandFromJSON + "' in request " + input );

            JSONObject answer = new JSONObject();
            answer.put( "error-source", "request" );
            answer.put( "error", "Unknown Command" );

            EventBusManager.fire( new CommandFinishedEvent( clock, commandFromJSON, interfaceName, Thread.currentThread().getId(), false ) );
            return Either.ofBadValue( answer );
        } else {
            log.debug( "Executing command with " + c );
            JSONObject ret = null;
            try {
                ret = c.execute( input, environment );

                EventBusManager.fire( new CommandFinishedEvent( clock, commandFromJSON, interfaceName, Thread.currentThread().getId(), true ) );
                return Either.ofGoodValue( ret );
            } catch( Exception e ) {
                log.warn( "Error in client request", e );

                JSONObject answer = new JSONObject();
                answer.put( "error-source", "internal" );
                answer.put( "error", e.getClass().getSimpleName() );
                answer.put( "message", e.getMessage() );

                EventBusManager.fire( new CommandFinishedEvent( clock, commandFromJSON, interfaceName, Thread.currentThread().getId(), false ) );
                return Either.ofBadValue( answer );
            } finally {
            }
        }

    }

    public static void throwIfErrorMessage( JSONObject response ) {
        if (response.has("error-source")
                && response.getString("error-source").equals("internal")
                && response.has("error")
                && response.has("message")) {
            throw new RequiredRequestFailedException(
                    response.getString("error")
                     + ": "
                     + response.getString("message") );
        }
    }

    public void addRequirements( Collection<String> dependencies ) {
        for ( Command<E> command : nameToCommand.values() ) {
            command.addRequirements( dependencies );
        }
    }
}
