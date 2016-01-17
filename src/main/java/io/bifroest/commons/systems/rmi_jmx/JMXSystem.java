package io.bifroest.commons.systems.rmi_jmx;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;
import org.slf4j.ext.XLogger;

import io.bifroest.commons.boot.interfaces.Subsystem;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.systems.SystemIdentifiers;
import io.bifroest.commons.systems.configuration.EnvironmentWithJSONConfiguration;

/**
 * Service class to start and stop JMX during runtime
 * 
 * @author Jörn Ahlers, sglimm
 */
@MetaInfServices
public class JMXSystem<E extends EnvironmentWithJSONConfiguration>
        implements Subsystem<E> {

    private static final XLogger log = LogService.getXLogger(JMXSystem.class);

    private Registry rmiRegistry;
    private JMXConnectorServer connector;
    private ServerSocketFactory serverSocketFactory;
    private String serviceUrl;
    private int rmiPort;
    private int jmxPort;
    private String jmxAccessFile;
    private String jmxPasswordFile;
    private String hostname;

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.RMIJMX;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Collections.emptyList();
    }

    @Override
    public void configure(JSONObject configuration) {
        JSONObject config = configuration.getJSONObject("rmi-jmx");
        rmiPort = config.getInt("rmiport");
        jmxPort = config.getInt("jmxport");
        jmxAccessFile = config.getString("accessfile");
        jmxPasswordFile = config.getString("passwordfile");   
        hostname = config.getString("hostname");
    }

    /**
     * Starts the jmx server with specified hostname, ports and access/password
     * files
     *
     * @param environment
     * @throws java.io.IOException
     */
    @Override
    public void boot(E environment) throws IOException {
        File aFile = new File(jmxAccessFile);
        File pFile = new File(jmxPasswordFile);

        if (!aFile.isFile() || !aFile.canRead()
                || !pFile.isFile() || !pFile.canRead()) {
            throw new IOException("Cannot read access and/or password file");
        }        

        if (rmiPort <= 0 || jmxPort <= 0) {
            throw new IOException("Ports must be positive.");
        }

        if (connector != null) {
            return; // there is already an existing connector
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(hostname);
        } catch (UnknownHostException ex) {
            throw new IOException(ex);
        }

        serverSocketFactory = new ServerSocketFactory(address);

        if (rmiRegistry == null) {
            try {
                rmiRegistry = LocateRegistry.createRegistry(rmiPort, null, serverSocketFactory);
            } catch (RemoteException ex) {
                throw new IOException(ex);
            }
        }

        serviceUrl = "service:jmx:rmi://"
                + address.getHostAddress() + ":" + jmxPort
                + "/jndi/rmi://"
                + address.getHostAddress() + ":" + rmiPort
                + "/jmxrmi";
        JMXServiceURL url;
        try {
            url = new JMXServiceURL(serviceUrl);
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        Map<String, Object> env = new HashMap<>();
        env.put("jmx.remote.x.authenticate", true);
        env.put("jmx.remote.x.local.only", false);
        env.put("jmx.remote.x.access.file", jmxAccessFile);
        env.put("jmx.remote.x.password.file", jmxPasswordFile);

        try {
            connector = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbeanServer);
        } catch (IOException ex) {
            throw new IOException(ex);
        }
                
        connector.start();
        log.info("Created JMX connection on " + serviceUrl);
    }

    /**
     * Stops the jmx server and all connections that are currently active. Frees
     * up the bound port.
     *
     * @param environment
     */
    @Override
    public void shutdown(E environment) {
        if (connector != null) {
            try {
                connector.stop();
            } catch (IOException e) { /* We have done what we could */

            }
            connector = null;
        }
        if (rmiRegistry != null) {
            try {
                UnicastRemoteObject.unexportObject(rmiRegistry, true);
            } catch (NoSuchObjectException e) {
                log.warn("", e);
            }
            rmiRegistry = null;
        }
        if (serverSocketFactory != null
                && serverSocketFactory.getLast() != null) {
            try {
                serverSocketFactory.getLast().close();
            } catch (IOException e) {
                log.catching(e);
            }
            serverSocketFactory = null;
        }
    }
}
