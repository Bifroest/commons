package io.bifroest.commons.net.jsonserver.commands;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import io.bifroest.commons.boot.EnvironmentWithInit;
import io.bifroest.commons.net.jsonserver.Command;

@MetaInfServices
public class ShutdownCommand< E extends EnvironmentWithInit > implements Command<E> {
    @Override
    public String getJSONCommand() {
        return "shutdown";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, final E environment ) {
        new Thread( "asynchronous Shutdown" ) {
            @Override
            public synchronized void run() {
                environment.initD().shutdown();
            }
        }.start();
        return new JSONObject();
    }
}
