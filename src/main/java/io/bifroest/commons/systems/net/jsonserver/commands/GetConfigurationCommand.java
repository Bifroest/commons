package io.bifroest.commons.systems.net.jsonserver.commands;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import io.bifroest.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import io.bifroest.commons.systems.net.jsonserver.Command;

@MetaInfServices
public class GetConfigurationCommand< E extends EnvironmentWithJSONConfiguration > implements Command<E> {
    @Override
    public String getJSONCommand() {
        return "get-configuration";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        return environment.getConfiguration();
    }

}
