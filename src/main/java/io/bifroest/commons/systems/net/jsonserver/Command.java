package io.bifroest.commons.systems.net.jsonserver;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import io.bifroest.commons.boot.interfaces.Environment;

public interface Command< E extends Environment > {
    String getJSONCommand();

    List<Pair<String, Boolean>> getParameters();

    JSONObject execute( JSONObject input, E environment );

    default void addRequirements( Collection<String> dependencies ) {};
}
