package io.bifroest.commons.configuration;

import org.json.JSONObject;

public interface ConfigurationObserver {
	void handleNewConfig( JSONObject conf );
}
