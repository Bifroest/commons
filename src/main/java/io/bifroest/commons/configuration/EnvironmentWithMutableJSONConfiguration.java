package io.bifroest.commons.configuration;

import org.json.JSONObject;

public interface EnvironmentWithMutableJSONConfiguration extends EnvironmentWithJSONConfiguration {

	void setConfiguration( JSONObject object );

	void setConfigurationLoader( JSONConfigurationLoader loader );

}
