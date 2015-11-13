package com.goodgame.profiling.commons.systems.net.wamp;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.util.json.JSONUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

@MetaInfServices
public class WampConnectionSystem<E extends EnvironmentWithJSONConfiguration & EnvironmentWithMutableWampClients> implements Subsystem<E> {
    private static final Logger log = LogService.getLogger(WampConnectionSystem.class);

    private final Map<String, JSONObject> realmConfigs = new HashMap<>();
    private final Map<String, WampClient> clients = new HashMap<>();
    
    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.WAMP_CLIENTS;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Collections.emptyList();
    }

    @Override
    public void configure(JSONObject configuration) {
        JSONObject sysConfig = configuration.getJSONObject( "wamp" );
        JSONObject defaultConfig = sysConfig.getJSONObject( "default" );
        
        JSONObject allRealmConfigs = sysConfig.getJSONObject( "realms" );
        if ( allRealmConfigs.keySet() != null ) {
            for ( String realm : allRealmConfigs.keySet() ) {
                JSONObject fullConfig = JSONUtils.deepMergeObjectsUseLast(defaultConfig, allRealmConfigs.getJSONObject( realm ));
                realmConfigs.put( realm, fullConfig);
                
            }
        }
    }

    @Override
    public void boot(E environment) throws Exception {
        for( String realm : realmConfigs.keySet() ) {
            log.info( "Adding Wamp Client for realm {}", realm );
            JSONObject realmConfig = realmConfigs.get( realm );
            WampClientBuilder builder = new WampClientBuilder();
            builder.withUri(realmConfig.getString( "router" ) );
            builder.withRealm(realm);
            // TODO: Authentication
            builder.withInfiniteReconnects();
            builder.withReconnectInterval(1, TimeUnit.SECONDS);
            environment.addWampClient( realm, builder.build() );
            clients.put( realm, builder.build() );
        }
        log.info( "All Wamp Clients created" );
    }

    @Override
    public void shutdown(E environment) {
        clients.forEach((k, v) -> v.close());
    }
    
}
