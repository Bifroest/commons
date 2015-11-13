package com.goodgame.profiling.commons.systems.net.socket;

import java.io.IOException;

import com.goodgame.profiling.commons.systems.net.socket.ReadWriteableClientSocket;

public interface ServerSocketHolder {
	public ReadWriteableClientSocket accept() throws IOException;
	public void close() throws IOException;

}
