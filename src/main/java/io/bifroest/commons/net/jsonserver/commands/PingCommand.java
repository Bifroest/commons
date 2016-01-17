package io.bifroest.commons.net.jsonserver.commands;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.net.jsonserver.Command;

@MetaInfServices
public class PingCommand implements Command<Environment> {
    @Override
    public String getJSONCommand() {
        return "ping";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, Environment environment ) {
        return new JSONObject().put( "ping", "pong" );
    }
}
