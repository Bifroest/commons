package io.bifroest.commons.net;

import java.util.Collection;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.net.socket.ReadWriteableClientSocket;

public interface IncomingConnectionHandlerFactory<E extends Environment> {
    IncomingConnectionHandler create( E environment, ReadWriteableClientSocket socket );
    default void shutdown() {};
    void addRequirements( Collection<String> dependencies );
}
