package io.bifroest.commons.net;

import org.json.JSONObject;

import io.bifroest.commons.configuration.EnvironmentWithJSONConfiguration;

public interface IncomingConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> {
    String handledFormat();
    IncomingConnectionHandlerFactory<E> createFactory( JSONObject interfaceConfig );
}
