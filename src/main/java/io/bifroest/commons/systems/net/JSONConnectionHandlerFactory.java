package io.bifroest.commons.systems.net;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.ext.XLogger;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.systems.net.jsonserver.Command;
import io.bifroest.commons.systems.net.jsonserver.CommandGroup;
import io.bifroest.commons.systems.net.socket.ReadWriteableClientSocket;

public final class JSONConnectionHandlerFactory<E extends Environment> implements IncomingConnectionHandlerFactory<E> {
    private static final XLogger log = LogService.getXLogger(JSONConnectionHandlerFactory.class);

    private final CommandGroup<E> commands;
    private final String interfaceName;

    public JSONConnectionHandlerFactory( JSONObject config, String interfaceName, Map<String, Command<E>> allCommands ) {
        this.interfaceName = Objects.requireNonNull( interfaceName );
        this.commands = Objects.requireNonNull( findCommands( config, allCommands ) );
    }

    @Override
    public JSONConnectionHandler<E> create( E environment, ReadWriteableClientSocket socket ) {
        return new JSONConnectionHandler<>( environment, socket, interfaceName, commands );
    }

    private CommandGroup<E> findCommands( JSONObject config, Map<String, Command<E>> allCommands ) {
        log.entry( config, allCommands );

        CommandGroup<E> group = new CommandGroup<>( interfaceName );

        if ( config.optString( "commands" ).equalsIgnoreCase( "all" ) ) {
            allCommands.forEach( (commandName, command) -> group.add( command ) );
        } else if ( config.optJSONArray( "commands" ) != null ) {
            for( int i = 0; i < config.getJSONArray( "commands" ).length(); i++ ) {
                String commandName = config.getJSONArray( "commands" ).getString( i );

                group.add( allCommands.get( commandName ) );
            }
        } else {
            throw new IllegalArgumentException( "What do you mean by " + config.get( "commands" ) );
        }
        log.exit( group );
        return group;
    }

    @Override
    public void addRequirements( Collection<String> dependencies ) {
        commands.addRequirements( dependencies );
    }
}
