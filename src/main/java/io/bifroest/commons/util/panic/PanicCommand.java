package io.bifroest.commons.util.panic;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.systems.net.jsonserver.Command;

@MetaInfServices
public class PanicCommand<E extends Environment> implements Command<E> {
    @Override
    public String getJSONCommand() {
        return "panic";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        ProfilingPanic.INSTANCE.panic();

        return new JSONObject();
    }
}
