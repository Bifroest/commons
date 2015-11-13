package com.goodgame.profiling.commons.systems.net.wamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.JSONConnectionHandlerFactoryFactory;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.Request;
import ws.wamp.jawampa.WampClient;

@MetaInfServices
public class WampRpcSystem<E extends EnvironmentWithJSONConfiguration & EnvironmentWithWampClients> implements Subsystem<E> {

    private static final Logger log = LogService.getLogger(JSONConnectionHandlerFactoryFactory.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, Map<String, Command<E>>> realmBindings = new HashMap<>();

    private final Map<String, WampClient> wampClientsForRealm = new HashMap<>();

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.WAMP_RPC_METHODS;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList(SystemIdentifiers.WAMP_CLIENTS);
    }

    private Set<String> nullSafeKeys(JSONObject in) {
        if (in.keySet() != null) {
            return in.keySet();
        } else {
            return Collections.emptySet();
        }
    }

    private Map<String, Command<E>> findCommands() {
        @SuppressWarnings("unchecked")
        Map<String, Command<E>> commands = new HashMap<>();
        for (Command<E> command : ServiceLoader.load(Command.class)) {
            commands.put(command.getJSONCommand(), command);
        }
        return commands;
    }

    @Override
    public void configure(JSONObject configuration) {
        JSONObject systemConfiguration = configuration.getJSONObject("wamp");
        log.debug("Configuring from object {}", systemConfiguration);

        JSONObject realmBindingsFromConfig = systemConfiguration.getJSONObject("rpc-methods");

        Map<String, Command<E>> commands = findCommands();
        for (String realm : nullSafeKeys(realmBindingsFromConfig)) {
            JSONObject thisRealmBindings = realmBindingsFromConfig.getJSONObject(realm);
            for (String commandJsonKey : nullSafeKeys(thisRealmBindings)) {
                String rpcName = thisRealmBindings.getString(commandJsonKey);
                if (!realmBindings.containsKey(realm)) {
                    realmBindings.put(realm, new HashMap<>());
                }

                Command<E> boundCommand = commands.get(commandJsonKey);
                if (boundCommand == null) {
                    throw new IllegalArgumentException("cannot find command " + commandJsonKey);
                }

                log.info("Planning to bind command {} to realm {} with name {}", boundCommand.getClass().toString(), realm, rpcName);
                realmBindings.get(realm).put(rpcName, boundCommand);
            }
        }
    }

    @Override
    public void boot(E environment) throws Exception {
        for (String realm : realmBindings.keySet()) {
            WampClient client = environment.getWampClientForRealm(realm);

            wampClientsForRealm.put(realm, client);
            client.statusChanged()
                  .subscribe( newStatus -> {
                      if (newStatus == WampClient.Status.CONNECTED) {
                          onClientIsConnected(realm, client, environment);
                      } else if (newStatus == WampClient.Status.DISCONNECTED) {
                          log.info("Wamp RPC for realm {} is now disconnected", realm);
                      }
                  });
            client.open();
        }
    }

    private void onClientIsConnected(String realm, WampClient client, E environment) {
        realmBindings.get(realm).forEach( ( rpcName, command ) -> {
            log.info("Registering {} as <{}> in realm <{}>",
                     command.getClass().toString(),
                     rpcName,
                     realm);
            
            client.startRegisteringProcedure(rpcName).onInvocation(
                  request -> processRequest( command, rpcName, environment, request) )
                    .onError( error -> log.warn("Couldn't register", error))
                    .onFinished( () -> log.info("Registering procedure finished"))
                    .register();
        });
    }

    private void processRequest(Command<E> rpcCommand, String command, E environment, Request request) {
        try {
            JSONObject ourArguments = jacksonToOrgJson(request.keywordArguments());
            JsonNode convertedResult = orgJsonTojackson(rpcCommand.execute(ourArguments, environment));

            // be over the top explicit to distinguish between reply( Object...) 
            // and reply( ArrayNode, ObjectNode )
            request.reply((ArrayNode) null, (ObjectNode) convertedResult);
        } catch (Exception e) {
            log.warn("Error while processing client", e);
            try {
                request.replyError(new ApplicationError(e.getMessage()));
            } catch (ApplicationError e2) {
                log.error("Cannot send error reply {} to client due to {} ", e, e2);
                log.error("First exception: ", e);
                log.error("Wamp error: ", e2);
            }
        }
    }

    private static JSONObject jacksonToOrgJson(ObjectNode arg) {
        try {
            return new JSONObject(mapper.writeValueAsString(arg));
        } catch (JsonProcessingException e) {
            log.warn("Cannot convert JSON - reverting to empty arguments: " + e);
            return new JSONObject();
        }
    }

    private static JsonNode orgJsonTojackson(JSONObject result) throws IOException {
        return mapper.readTree(result.toString());
    }

    @Override
    public void shutdown(E environment) {
        // jawampa doesn't support unregistering.
    }

}
