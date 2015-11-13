package com.goodgame.profiling.commons.systems.net.multiserver;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.IncomingConnectionHandlerFactory;
import com.goodgame.profiling.commons.systems.net.IncomingConnectionHandlerFactoryFactory;
import com.goodgame.profiling.commons.systems.net.ServerThread;
import com.goodgame.profiling.commons.systems.net.throttle.LinearThrottleControl;
import com.goodgame.profiling.commons.systems.net.throttle.TimeBasedSensor;

@MetaInfServices
public class MultiServerSystem< E extends EnvironmentWithJSONConfiguration >
implements Subsystem<E> {

    private static final Clock clock = Clock.systemUTC();

    private final List<ServerThread<E>> serverThreads = new ArrayList<>();

    private final List<String> requiredSystems = new ArrayList<>();

    // name -> factory
    private Map<String, IncomingConnectionHandlerFactory<E>> factories;
    // name -> config
    private Map<String, JSONObject> interfaceConfigs;

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.MULTI_SERVER;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return requiredSystems;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void configure(JSONObject configuration) {
        JSONArray interfaces = configuration.getJSONObject( "multi-server" ).getJSONArray( "interfaces" );

        factories = new HashMap<>();
        interfaceConfigs = new HashMap<>();
        for ( IncomingConnectionHandlerFactoryFactory<E> factoryfactory : ServiceLoader.load(IncomingConnectionHandlerFactoryFactory.class) ) {
            for ( int i = 0; i < interfaces.length(); i++ ) {
                JSONObject interfaceConfig = interfaces.getJSONObject( i );
                if( interfaceConfig.getString( "type" ).equals( "tcp" ) || interfaceConfig.getString( "type" ).equals( "unix socket" ) ) {
                    IncomingConnectionHandlerFactory<E> factory = factoryfactory.createFactory( interfaceConfig );
                    factories.put( interfaceConfig.getString( "name" ), factory );
                    interfaceConfigs.put( interfaceConfig.getString( "name" ), interfaceConfig );
                    factory.addRequirements( requiredSystems );
                } else {
                    throw new IllegalArgumentException( "Cannot handle " + interfaceConfig.getString( "type" ) );
                }
            }
        }
        requiredSystems.add(SystemIdentifiers.STATISTICS);
    }

    @Override
    public void boot(E environment) throws Exception {
        for ( String interfaceName : factories.keySet() ) {
            ServerThread<E> serverThread = new ServerThread<E>(
                    environment,
                    interfaceConfigs.get( interfaceName ),
                    new LinearThrottleControl( new TimeBasedSensor( clock, clock.instant(), Duration.ofMinutes( 1 ) ) ),
                    factories.get( interfaceName )
            );
            serverThreads.add( serverThread );
        }

        this.serverThreads.stream().forEach((serverThread) -> {
            serverThread.start();
        });
    }

    @Override
    public void shutdown(E environment) {
        this.serverThreads.stream().forEach((thread) -> {
            thread.shutdown();
        });
    }
}
