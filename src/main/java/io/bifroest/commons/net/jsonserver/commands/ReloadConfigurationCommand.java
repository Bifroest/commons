/*
 * Copyright 2014 Goodgame Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.bifroest.commons.net.jsonserver.commands;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.configuration.EnvironmentWithJSONConfiguration;
import io.bifroest.commons.net.jsonserver.Command;

@MetaInfServices
public class ReloadConfigurationCommand< E extends EnvironmentWithJSONConfiguration>
        implements Command<E> {
    private static final Logger log = LogService.getLogger(ReloadConfigurationCommand.class);

    @Override
    public String getJSONCommand() {
        return "reload-config";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute(JSONObject input, E environment) {
        log.debug("Reloading configuration");
        log.debug("Old config: {}", environment.getConfiguration().toString());
        environment.getConfigurationLoader().loadConfiguration();
        log.debug("New config: {}", environment.getConfiguration().toString());
        return environment.getConfiguration();
    }
}
