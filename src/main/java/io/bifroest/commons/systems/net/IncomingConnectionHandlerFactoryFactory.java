package io.bifroest.commons.systems.net;

import org.json.JSONObject;

import io.bifroest.commons.systems.configuration.EnvironmentWithJSONConfiguration;

public interface IncomingConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> {
    String handledFormat();
    IncomingConnectionHandlerFactory<E> createFactory( JSONObject interfaceConfig );
}
