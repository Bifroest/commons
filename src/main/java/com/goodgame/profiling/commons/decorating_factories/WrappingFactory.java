package com.goodgame.profiling.commons.decorating_factories;

import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface WrappingFactory<E extends Environment, T> {
    List<Class<? super E>> getRequiredEnvironments();
    default void addRequiredSystems( Collection<String> requiredSystems, JSONObject subconfiguration ) { }

    String handledType();
    T wrap( E environment, T inner, JSONObject subconfiguration );
}
