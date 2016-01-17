package io.bifroest.commons.systems.net;

import java.util.Collection;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.systems.net.socket.ReadWriteableClientSocket;

public interface IncomingConnectionHandlerFactory<E extends Environment> {
    IncomingConnectionHandler create( E environment, ReadWriteableClientSocket socket );
    default void shutdown() {};
    void addRequirements( Collection<String> dependencies );
}
