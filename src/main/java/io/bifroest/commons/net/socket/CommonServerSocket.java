package io.bifroest.commons.net.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class CommonServerSocket implements ServerSocketHolder {

	private ServerSocket serverSocket;

	public CommonServerSocket( int port ) throws IOException{
		serverSocket = new ServerSocket( port );
	}
	
	public CommonServerSocket( int port, int backlog ) throws IOException{
		serverSocket = new ServerSocket( port, backlog );
	}
	
	public CommonServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException{
		serverSocket = new ServerSocket( port, backlog, bindAddr );
	}
	
	@Override
	public ReadWriteableClientSocket accept() throws IOException {
		Socket client = serverSocket.accept();
		return new CommonClientServerWrapper( client );
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
		
	}
	
	private class CommonClientServerWrapper implements ReadWriteableClientSocket {
		Socket client;

		public CommonClientServerWrapper ( Socket client ) {
			this.client = client;
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return client.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return client.getOutputStream();
		}

		@Override
		public void close() throws IOException {
			client.close();
		}
		
		public boolean isClosed() {
			return client.isClosed();
		}

		@Override
		public String getTypeWithInfo() {
			return "CommonClientSocket with remote address '" + Objects.toString( client.getRemoteSocketAddress() ) + "'";
		}
		
	}

}
