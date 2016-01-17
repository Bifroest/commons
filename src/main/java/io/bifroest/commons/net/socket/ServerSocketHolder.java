package io.bifroest.commons.net.socket;

import java.io.IOException;

import io.bifroest.commons.net.socket.ReadWriteableClientSocket;

public interface ServerSocketHolder {
	public ReadWriteableClientSocket accept() throws IOException;
	public void close() throws IOException;

}
