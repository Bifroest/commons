package io.bifroest.commons.boot.interfaces;

import java.util.Collection;

import org.json.JSONObject;

public interface Subsystem<E extends Environment> {

    /**
     * This returns the identifier string for this system.
     *
     * The bootloader will use this to output information, and other systems
     * will use this identifier to require this system. Due to the latter, it is
     * advisable to provide this identifier with some public constant in an
     * actual system.
     */
    String getSystemIdentifier();

    /**
     * This returns the systems that have to be booted before this.
     *
     * The systems will be loaded in no guaranteed order, so nothing should
     * depend on that order. System identifiers unknown to the boot loader will
     * cause errors and the boot loader will not start up the program.
     */
    Collection<String> getRequiredSystems();

    /**
     * Validates the configuration found in environment
     *
     * @param configuration     
     */
    void configure( JSONObject configuration );

    /**
     * This starts the system and adds it's contributions to the environment.
     *
     * The system can assume that contributions to the environment by required
     * systems are initialized in the environment. Other parts of the
     * environment are not in a defined state, and usually they will be null.
     * However, they might be partially initialized and calling methods on the
     * cause a horrible mess. In short: Only get things from the environment you
     * required in getRequiredSystems.
     */
    void boot( E environment ) throws Exception;

    /**
     * Called when the system is shutting down.
     *
     * Clean up after yourself, ideally unset your parts and contributions to
     * the environment to force others to require the required systems
     * officially and all in all, hurry up, because we are about to be killed by
     * an impatient operator.
     */
    void shutdown( E environment );
}
