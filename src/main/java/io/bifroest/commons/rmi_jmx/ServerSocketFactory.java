package io.bifroest.commons.rmi_jmx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import org.slf4j.Logger;

import io.bifroest.commons.logging.LogService;

/**
 * Factory for a ServerSocket which is required to create a rmi registry.
 * 
 * @author Jörn Ahlers, sglimm
 */
public class ServerSocketFactory implements RMIServerSocketFactory {
    private static final Logger log = LogService.getLogger(ServerSocketFactory.class);

	private final InetAddress address;
	private ServerSocket lastSocket = null;

	public ServerSocketFactory(InetAddress address) {
		this.address = address;
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		if (lastSocket != null) {
			log.warn("Creating multiple sockets with one ServerSocketFactory - is this right?");
		}
		
		lastSocket = new ServerSocket(port, 0, this.address);
		return lastSocket;
	}
	
	public ServerSocket getLast() {
		return lastSocket;
	}

        @Override
	public boolean equals(Object obj) {
		if ((obj == null) || (super.getClass() != obj.getClass())) {
			return false;
		}
		ServerSocketFactory other = (ServerSocketFactory) obj;
		if (address == null) {
			return (other.address == null);
		}
		return address.equals(other.address);
	}

        @Override
	public int hashCode() {
		return this.address == null ? 0 : this.address.hashCode();
	}
}
