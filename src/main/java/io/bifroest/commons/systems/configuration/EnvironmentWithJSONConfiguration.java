package io.bifroest.commons.systems.configuration;

import org.json.JSONObject;

import io.bifroest.commons.boot.interfaces.Environment;

public interface EnvironmentWithJSONConfiguration extends Environment {

	JSONObject getConfiguration();

	JSONConfigurationLoader getConfigurationLoader();

}
