package io.bifroest.commons.boot;

import io.bifroest.commons.boot.BootLoaderNG;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.ConfigurationException;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;

import io.bifroest.commons.boot.interfaces.Subsystem;
import io.bifroest.commons.exception.SubsystemNotFoundException;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.systems.common.EnvironmentWithConfigPath;
import io.bifroest.commons.systems.configuration.EnvironmentWithMutableJSONConfiguration;

public class BootLoaderNGTest {

    private interface MinimalEnvironment extends EnvironmentWithConfigPath, EnvironmentWithMutableJSONConfiguration {}

    private static MinimalEnvironment ENVIRONMENT;
    private static BootLoaderNG<MinimalEnvironment> INSTANCE;
    private static Subsystem<MinimalEnvironment> SUB_SYSTEM_A;
    private static Subsystem<MinimalEnvironment> SUB_SYSTEM_B;
    private static Subsystem<MinimalEnvironment> SUB_SYSTEM_C;
    private static Logger LOGGER;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public BootLoaderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        LOGGER = LogService.getLogger();
    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() throws ConfigurationException {
        //LOGGER.info("*** setUp(): creating new bootloader and environment ***");
        ENVIRONMENT = mock( MinimalEnvironment.class );
        when(ENVIRONMENT.getConfigPath()).thenReturn( Paths.get("test-config" ) );
        when(ENVIRONMENT.getConfiguration()).thenReturn( new JSONObject() );
        INSTANCE = new BootLoaderNG<>(ENVIRONMENT, false);
        SUB_SYSTEM_A = TestSystemFactory.createSubsystem("system.a", Collections.emptyList());
        SUB_SYSTEM_B = TestSystemFactory.createSubsystem("system.b", Arrays.asList(SUB_SYSTEM_A.getSystemIdentifier()));
        SUB_SYSTEM_C = TestSystemFactory.createSubsystem("system.c", Arrays.asList(SUB_SYSTEM_B.getSystemIdentifier()));
    }

    @After
    public void tearDown() {
        INSTANCE.shutdown();
    }

    /**
     * Test of enableSubsystem method, of class BootLoaderNG.
     *
     * @throws
     * io.bifroest.commons.exception.SubsystemNotFoundException
     * @throws io.bifroest.commons.exception.ConfigurationException
     */
    @Ignore
    @Test
    public void testEnableSubsystemWithAvailableSystem()
            throws SubsystemNotFoundException, ConfigurationException {
        LOGGER.info("Test: testEnableSubsystemWithAvailableSystem()");
        INSTANCE.addSubsystem(SUB_SYSTEM_A);
        INSTANCE.enableSubsystem(SUB_SYSTEM_A.getSystemIdentifier());
        List<Subsystem<MinimalEnvironment>> subsystems = INSTANCE.getSystemsToBoot();
        boolean found = false;
        for (Subsystem<MinimalEnvironment> system : subsystems) {
            if (system.getSystemIdentifier().equals(SUB_SYSTEM_A.getSystemIdentifier())) {
                found = true;
            }
        }
        assertTrue("System " + SUB_SYSTEM_A.getSystemIdentifier() + " not found.", found);
    }

    /**
     * Test of enableSubsystem method, of class BootLoaderNG.
     *
     * @throws
     * io.bifroest.commons.exception.SubsystemNotFoundException
     * @throws io.bifroest.commons.exception.ConfigurationException
     */
    @Ignore
    @Test
    public void testEnableSubsystemWithoutAvailableSystem()
            throws SubsystemNotFoundException, ConfigurationException {
        LOGGER.info("Test: testEnableSubsystemWithoutAvailableSystem()");
        exception.expect(SubsystemNotFoundException.class);
        INSTANCE.enableSubsystem("blablub-system");
    }

    /**
     * Test of enableSubsystem method, of class BootLoaderNG.
     *
     * @throws
     * io.bifroest.commons.exception.SubsystemNotFoundException
     * @throws io.bifroest.commons.exception.ConfigurationException
     */
    @Ignore
    @Test
    public void testEnableSubsystemWithAvailableSystemAndAvailableDependencies()
            throws SubsystemNotFoundException, ConfigurationException {
        LOGGER.info("Test: testEnableSubsystemWithAvailableSystemAndAvailableDependencies()");
        INSTANCE.addSubsystem(SUB_SYSTEM_A);
        INSTANCE.addSubsystem(SUB_SYSTEM_B);
        INSTANCE.addSubsystem(SUB_SYSTEM_C);
        INSTANCE.enableSubsystem(SUB_SYSTEM_C.getSystemIdentifier());
        final List<Subsystem<MinimalEnvironment>> subsystems = INSTANCE.getSystemsToBoot();
        final List<String> subsystemIdentifier = new ArrayList<>();
        final List<String> dependencies = new ArrayList<>();
        boolean found = true;
        subsystems.stream().forEach((system) -> {
            subsystemIdentifier.add(system.getSystemIdentifier());
            if (system.getSystemIdentifier().equals(SUB_SYSTEM_C.getSystemIdentifier())) {
                Collection<String> col = system.getRequiredSystems();
                col.stream().forEach((s) -> {
                    dependencies.add(s);
                });
            }
        });

        if (dependencies.isEmpty()) {
            fail("No dependencies found. This should not happen.");
        }

        for (String s : dependencies) {
            if (!subsystemIdentifier.contains(s)) {
                found = false;
            }
        }
        assertTrue("One or more dependencies not found for system " + SUB_SYSTEM_C.getSystemIdentifier(), found);
    }

    /**
     * Test of getBootOrder method, of class BootLoaderNG.
     *
     * @throws java.lang.Exception
     */
    @Ignore
    @Test
    public void testGetBootOrder() throws Exception {
        LOGGER.info("Test: testGetBootOrder()");

        INSTANCE.addSubsystem(SUB_SYSTEM_A);
        INSTANCE.addSubsystem(SUB_SYSTEM_B);
        INSTANCE.addSubsystem(SUB_SYSTEM_C);

        List<String> expectedIdentifier = new ArrayList<>();
        expectedIdentifier.add(SUB_SYSTEM_A.getSystemIdentifier());
        expectedIdentifier.add(SUB_SYSTEM_B.getSystemIdentifier());
        expectedIdentifier.add(SUB_SYSTEM_C.getSystemIdentifier());
        INSTANCE.enableSubsystem(SUB_SYSTEM_C.getSystemIdentifier());

        //get boot order with reflection
        Method method = BootLoaderNG.class.getDeclaredMethod("getBootOrder", new Class[]{});
        method.setAccessible(true);
        Object result = method.invoke(INSTANCE, new Object[]{});

        @SuppressWarnings("unchecked")
        List<Subsystem<MinimalEnvironment>> subsystems = (List<Subsystem<MinimalEnvironment>>) result;
        List<String> systemIdentifier = new ArrayList<>();
        subsystems.stream().forEach((subsystem) -> {
            systemIdentifier.add(subsystem.getSystemIdentifier());
        });

        assertTrue("Expected systems to boot differ from real systems to boot",
                expectedIdentifier.size() == systemIdentifier.size());
        assertTrue("The order is different or some unknown systems are booting",
                expectedIdentifier.equals(systemIdentifier));
    }

    private static class TestSystemFactory {

        static Subsystem<MinimalEnvironment> createSubsystem(String identifier, Collection<String> requiredSystems) {
            return new Subsystem<MinimalEnvironment>() {

                @Override
                public String getSystemIdentifier() {
                    return identifier;
                }

                @Override
                public Collection<String> getRequiredSystems() {
                    return requiredSystems;
                }

                @Override
                public void boot(MinimalEnvironment environment) throws Exception {
                }

                @Override
                public void shutdown(MinimalEnvironment environment) {
                }

                @Override
                public void configure(JSONObject configuration) {
                }
            };
        }
    }
}
