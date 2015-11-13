package com.goodgame.profiling.commons.systems.net;

import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;

public interface IncomingConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> {
    String handledFormat();
    IncomingConnectionHandlerFactory<E> createFactory( JSONObject interfaceConfig );
}
