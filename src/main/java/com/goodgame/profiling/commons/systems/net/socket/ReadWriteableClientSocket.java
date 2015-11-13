package com.goodgame.profiling.commons.systems.net.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ReadWriteableClientSocket {

	public InputStream getInputStream() throws IOException;
	
	public OutputStream getOutputStream() throws IOException;
	
	public void close() throws IOException;
	
	public String getTypeWithInfo();
	
	public boolean isClosed();

}
