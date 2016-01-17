package io.bifroest.commons.systems.net;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import io.bifroest.commons.systems.net.jsonserver.Command;

@MetaInfServices
public final class JSONConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> 
    implements IncomingConnectionHandlerFactoryFactory<E> {

    private static final Logger log = LogService.getLogger(JSONConnectionHandlerFactoryFactory.class);

    private Map<String, Command<E>> allCommands; // quasi-final

    @Override
    public String handledFormat() {
        return "json";
    }

    @Override
    public JSONConnectionHandlerFactory<E> createFactory( JSONObject interfaceConfig ) {
        synchronized ( this ) {
            if ( allCommands == null ) {
                this.allCommands = createAllCommands( );
            }
        }
        return new JSONConnectionHandlerFactory<>( interfaceConfig, interfaceConfig.getString( "name" ), allCommands );
    }

    @SuppressWarnings( "unchecked" )
    private static < E extends Environment > Map<String, Command<E>> createAllCommands( ) {
        Map<String, Command<E>> commands = new HashMap<>();
        for ( Command<E> command : ServiceLoader.load( Command.class ) ) {
            log.info( "Adding " + command );
            commands.put( command.getJSONCommand(), command );
        }
        return commands;
    }
}
