package com.goodgame.profiling.commons.systems.net;

import java.util.Collection;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.net.socket.ReadWriteableClientSocket;

public interface IncomingConnectionHandlerFactory<E extends Environment> {
    IncomingConnectionHandler create( E environment, ReadWriteableClientSocket socket );
    default void shutdown() {};
    void addRequirements( Collection<String> dependencies );
}
