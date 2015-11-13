package com.goodgame.profiling.commons.systems.net.wamp;

import ws.wamp.jawampa.WampClient;

public interface EnvironmentWithMutableWampClients extends EnvironmentWithWampClients {
    public void addWampClient( String realm, WampClient client );
}
