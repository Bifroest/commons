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
package io.bifroest.commons.boot;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import io.bifroest.commons.boot.interfaces.Subsystem;
import io.bifroest.commons.exception.CircularDependencyException;
import io.bifroest.commons.exception.SubsystemNotFoundException;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.environment.EnvironmentWithConfigPath;
import io.bifroest.commons.configuration.ActualJSONConfigurationLoader;
import io.bifroest.commons.configuration.ConfigurationObserver;
import io.bifroest.commons.configuration.EnvironmentWithMutableJSONConfiguration;
import io.bifroest.commons.util.panic.PanicAnnouncement;
import io.bifroest.commons.util.panic.ProfilingPanic;

public final class BootLoaderNG<E extends EnvironmentWithConfigPath & EnvironmentWithMutableJSONConfiguration> implements InitD{
    private static final Logger log = LogService.getLogger(BootLoaderNG.class);

    private final E environment;
    private final Map<String, Subsystem<E>> systemsAvailable = new HashMap<>();
    private final List<Subsystem<E>> systemsToBoot = new ArrayList<>();
    private final List<Subsystem<E>> systemsRunning = new ArrayList<>();
    private final ActualJSONConfigurationLoader<E> loader;

    /**
     * Constructs a bootloader with supplied environment. Auto-search is enabled
     * by default.
     *
     * @param env A environment extented from @see Environment
     * @throws ConfigurationException
     */
    public BootLoaderNG(E env) throws ConfigurationException {
        this(env, true);
    }

    /**
     * Constructs a bootloader with supplied environment. If autoSearchSystems
     * is true, the bootloader tries to load the subsystems via SearviceLoader.
     * If set to false, the subsystems must be added through the addSubsystem()-
     * method
     *
     * @param env A environment extented from @see Environment
     * @param autoSearchSystems if true, autoload systems, false does not load
     * any system.
     * @throws ConfigurationException
     */
    public BootLoaderNG(E env, boolean autoSearchSystems) throws ConfigurationException {
        environment = env;
        loader = new ActualJSONConfigurationLoader<>(environment);
        environment.setConfigurationLoader(loader);
        this.loadConfiguration();
        registerOptionalStatusWriter( env, loader );
        if (autoSearchSystems) {
            findAvailableSubsystems();
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                shutdown();
            }
        });
        Thread.setDefaultUncaughtExceptionHandler( new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException( Thread t, Throwable e ) {
                log.error( "Exception in thread " + t.getName(), e );
            }
        } );

        ProfilingPanic.INSTANCE.addAction( new PanicAnnouncement() );
    }

    private void registerOptionalStatusWriter( E environment, ActualJSONConfigurationLoader<E> loader ) throws ConfigurationException {
        JSONObject configuration = environment.getConfiguration();
        if ( !configuration.has( "configuration" ) ) return;

        JSONObject configurationConfiguration = configuration.getJSONObject( "configuration" );
        if ( !configurationConfiguration.has( "status-file" ) ) return;

       ConfigurationObserver statusWriter = new ConfigurationLoadStatusWriter<E>( configurationConfiguration.getString( "status-file" ), loader );

       // write the status of the initial config load
       statusWriter.handleNewConfig( configuration );


       loader.subscribe( statusWriter );
    }

    private static class ConfigurationLoadStatusWriter<E extends EnvironmentWithConfigPath & EnvironmentWithMutableJSONConfiguration> implements ConfigurationObserver {
        private static final Logger log = LogService.getLogger(ConfigurationLoadStatusWriter.class);

        private final String destination;
        private final ActualJSONConfigurationLoader<E> loader;

        public ConfigurationLoadStatusWriter( String destination, ActualJSONConfigurationLoader<E> loader ) {
            this.destination = destination;
            this.loader = loader;
        }

        @Override
        public void handleNewConfig( JSONObject ignored ) {

            Path tmpFile = Paths.get( destination + "_new" );
            Path destinationFile = Paths.get( destination );

            log.debug( "Trying to write parse status to {} (tmp file used: {})", destinationFile, tmpFile );

            // We don't know if or when someoen reads the file, so atomic updates are necessary
            try {
                try ( BufferedWriter output = Files.newBufferedWriter( tmpFile, StandardOpenOption.CREATE_NEW ) ) {
                    computeJsonContent().write( output );
                }
                Files.move( Paths.get( destination + "_new" ), Paths.get( destination ), StandardCopyOption.REPLACE_EXISTING );
            } catch ( IOException e ) {
                log.warn( "Cannot write parse status temporarily to " + tmpFile + " and move it to " + destinationFile, e);
            }
        }
        private JSONObject computeJsonContent() {
            Map<Path, Optional<String>> newErrors = loader.getParseErrors();
            JSONObject statusFile = new JSONObject();
            JSONArray fileStati = new JSONArray();
            newErrors.entrySet()
                     .stream()
                     .map( e -> new JSONObject().put( "name", e.getKey().toString() )
                                                .put( "parse_error", e.getValue().orElse(null) ) )
                     .forEach( fileStati::put );
            statusFile.put( "files", fileStati );
            return statusFile;
        }
    }
    /**
     * Gets all systems which are booted when boot() is called.
     *
     * @return A list of systems
     */
    public List<Subsystem<E>> getSystemsToBoot() {
        return Collections.unmodifiableList(systemsToBoot);
    }

    /**
     * Gets a list of running services.
     *
     * @return A list of systems
     */
    public List<Subsystem<E>> getSystemsRunning() {
        return Collections.unmodifiableList(systemsRunning);
    }

    /**
     * Gets a list of all available systems.
     *
     * @return A list of systems
     */
    public List<Subsystem<E>> getSystemsAvailable() {
        return Collections.unmodifiableList(new ArrayList<>(systemsAvailable.values()));
    }

    /**
     * Adds a subsystem to the bootloader. If a system with the same identifier
     * already exists, it is replaced with the new one.
     *
     * @param system An object implementing the Subsystem interface
     */
    public void addSubsystem(Subsystem<E> system) {
        if (system != null && !system.getSystemIdentifier().isEmpty()) {
            systemsAvailable.put(system.getSystemIdentifier(), system);
        }
    }

    /**
     * Enables a system which will be booted when BootLoader.boot() is called.
     *
     * @param systemIdentifier
     * @throws SubsystemNotFoundException If the system is not available, an
     * exception is thrown.
     * @throws ConfigurationException If the system cannot be configured, an
     * exception is thrown
     */
    public void enableSubsystem(String systemIdentifier)
            throws SubsystemNotFoundException, ConfigurationException {
        if (systemIdentifier == null || systemIdentifier.isEmpty()) {
            throw new SubsystemNotFoundException("Cannot find subsystem for empty identifier.");
        }
        if (!systemsAvailable.containsKey(systemIdentifier)) {
            throw new SubsystemNotFoundException("Cannot find subsystem for identifier " + systemIdentifier);
        }
        Subsystem<E> system = systemsAvailable.get(systemIdentifier);
        systemsToBoot.add(system);
        log.info("Configuring system {}", systemIdentifier);
        system.configure( environment.getConfiguration() );

        this.addMissingSubsystems();
        log.info("Subsystem '" + systemIdentifier + "' enabled");
    }

    /**
     * Loads the JSON configuration in memory
     *
     * @throws ConfigurationException If no configuration is found an exception
     * is thrown.
     */
    private void loadConfiguration() throws ConfigurationException {
        loader.loadConfiguration();
    }

    /**
     * Finds all subsystems provided via ServiceLoader
     */
    @SuppressWarnings( "unchecked" )
    private void findAvailableSubsystems() {
        for( Subsystem<E> sub : ServiceLoader.load(Subsystem.class) ) {
            systemsAvailable.put(sub.getSystemIdentifier(), sub);
            log.debug("Found: " + sub.getSystemIdentifier());
        }
    }

    /**
     * Checks if a subsystem is missing. This can happen, if a systems needs
     * dependencies, which are not added or enabled so far. If such a missing
     * system is found it will be also be booted.
     *
     * @throws SubsystemNotFoundException If a required subsystem is not
     * available, this Exception is thrown
     */
    private void addMissingSubsystems() throws SubsystemNotFoundException, ConfigurationException {
        List<Subsystem<E>> temp = new ArrayList<>();
        for (Subsystem<E> subSystem : this.systemsToBoot) {
            Collection<String> requiredSystems = subSystem.getRequiredSystems();
            for (String requiredSystemIdentifier : requiredSystems) {
                if (!this.systemsAvailable.containsKey(requiredSystemIdentifier)) {
                    throw new SubsystemNotFoundException("Cannot find required subsystem '"
                            + requiredSystemIdentifier + "' for system '" + subSystem.getSystemIdentifier() + "'");
                }
                Subsystem<E> requiredSubsystem = this.systemsAvailable.get(requiredSystemIdentifier);
                if (!this.systemsToBoot.contains(requiredSubsystem)
                        && !temp.contains(requiredSubsystem)) {
                    temp.add(systemsAvailable.get(requiredSystemIdentifier));
                    log.debug("Added missing subsystem " + requiredSystemIdentifier);
                }
            }
        }
        if (!temp.isEmpty()) {
            for (Subsystem<E> s : temp) {
                log.info("Configuring missing dependency {}", s.getSystemIdentifier());
                s.configure(environment.getConfiguration());
            }
            this.systemsToBoot.addAll(temp);
        }
        // We check the available dependencies for all booting subsystems
        boolean missing = false;
        for (Subsystem<E> subsystem : this.systemsToBoot) {
            for (String s : subsystem.getRequiredSystems()) {
                if (!this.systemsAvailable.containsKey(s)) {
                    throw new SubsystemNotFoundException("Cannot find required subsystem '"
                            + s + "' for system '" + subsystem.getSystemIdentifier() + "'");
                }
                Subsystem<E> requiredSubsystem = this.systemsAvailable.get(s);
                if (!this.systemsToBoot.contains(requiredSubsystem)) {
                    missing = true;
                }
            }
        }
        if (missing) {
            addMissingSubsystems();
        }
    }

    /**
     * Calculates the systems boot order. This is an iterative process: At
     * first, from a list with all available systems, all systems with no
     * dependencies are removed. This is repeated until the list is empty. If
     * there are systems still remaining, there is a dependency misconfiguration
     * and a CircularDependencyException is raised.
     *
     * @return A list with systems ordered by boot priority. The first element
     * needs to start first, the second after and so on.
     * @throws CircularDependencyException If two or more systems are
     * misconfigured, a circular dependency can occur. This happens e.g. if
     * system A depends on system B and system B also requires system A. This
     * cannot be resolved and an exception is thrown.
     */
    private List<Subsystem<E>> getBootOrder() throws CircularDependencyException {
        HashMap<String, Subsystem<E>> bootSystems = new HashMap<>();
        HashMap<String, List<String>> systemDependencies = new HashMap<>();
        List<Subsystem<E>> result = new ArrayList<>();

        // shuffle systems to boot, so no one can forget system dependencies
        Collections.shuffle(this.systemsToBoot);

        this.systemsToBoot.stream().forEach((system) -> {
            bootSystems.put(system.getSystemIdentifier(), system);
            systemDependencies.put(system.getSystemIdentifier(),
                    system.getRequiredSystems().stream().filter( dep -> !dep.equals( system.getSystemIdentifier() ) ).collect( Collectors.toList() ) );
        });
        // while there are dependencies to solve
        while (!systemDependencies.isEmpty()) {
            // Get all nodes without any dependency            
            Set<String> keys = systemDependencies.keySet();
            List<String> resolved = new ArrayList<>();
            keys.stream().forEach((key) -> {
                log.trace( "Trying to resolve {}", key );
                Collection<String> dependencies = systemDependencies.get(key);
                log.trace( "Found dependencies: {}", dependencies );
                if (dependencies == null || dependencies.isEmpty()) {
                    log.trace( "Marking {} as resolved", key );
                    resolved.add(key);
                }
            });
            // if resolved is empty, we have a loop in the graph            
            if (resolved.isEmpty()) {
                String msg = "Loop in graph! This should not happen. Check your dependencies! Remaining systems: " + keys.toString();
                throw new CircularDependencyException(msg, systemDependencies);
            }

            // remove systemsToBoot found from dependency graph
            resolved.stream().forEach((systemIdentifier) -> {
                systemDependencies.remove(systemIdentifier);
                result.add(bootSystems.get(systemIdentifier));
            });

            // remove dependencies
            Set<String> systemDependenciesKeys = systemDependencies.keySet();
            systemDependenciesKeys.stream().map((key) -> systemDependencies.get(key)).forEach((values) -> {
                resolved.stream().forEach((resolvedValue) -> {
                    values.removeIf( v -> v.equals(resolvedValue) );
                });
            });
        }
        return result;
    }

    /**
     * Boots all enabled subsystems. After determining the boot order, the
     * needed configuration is checked. After this, the boot()-method from each
     * subsystem is called.
     */
    public void boot() {
        List<Subsystem<E>> systems;
        try {
            //Calculate the boot order
            systems = this.getBootOrder();
        } catch ( Exception e ) {
            log.error( "Cannot calculate boot order:", e );
            shutdown();
            return;
        }

        log.info("Booting up {} sytems: {}", systems.size(), this.systemList2String(systems));
        for (Subsystem<E> system : systems) {
            try {
                if (systemsRunning.contains(system)) {
                    log.info("Found already started service: " + system.getSystemIdentifier() + ". Skipping.");
                    continue;
                }
                log.info("Booting " + system.getSystemIdentifier());
                system.boot(this.environment);
                this.systemsRunning.add(system);
            } catch ( Exception e ) {
                log.error( "Exception while booting " + system.getSystemIdentifier(), e );
                shutdown();
                return;
            }
        }
        log.info( "Service startup successful." );
    }

    private String systemList2String(List<Subsystem<E>> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[ " + StringUtils.join( list.stream()
                                            .map( system -> system.getSystemIdentifier() )
                                            .toArray(),
                                        " -> " ) + " ]";
    }

    /**
     * Shuts down all booted systems. Systems will be shutdown in reverse boot
     * order
     *
     */
    @Override
    // synchronized, so that multiple shutdown hooks / commands cannot interfere with each other
    public synchronized void shutdown() {
        for (int i = systemsRunning.size() - 1; i >= 0; i--) {
            Subsystem<E> system = systemsRunning.get(i);
            log.info("Shutting down system " + system.getSystemIdentifier());
            system.shutdown(this.environment);
        }
        this.systemsRunning.clear();
        log.info("Shutdown complete.");
    }

}
