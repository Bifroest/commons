package io.bifroest.commons.net;

import java.io.IOException;
import java.net.SocketException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.json.JSONException;
import org.json.JSONObject;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.statistics.SimpleProgramStateTracker;
import io.bifroest.commons.statistics.units.TIME_UNIT;
import io.bifroest.commons.cron.TaskRunner;
import io.bifroest.commons.cron.TaskRunner.TaskID;
import io.bifroest.commons.net.jsonserver.CommandMonitor;
import io.bifroest.commons.net.socket.CommonServerSocket;
import io.bifroest.commons.net.socket.ReadWriteableClientSocket;
import io.bifroest.commons.net.socket.ServerSocketHolder;
import io.bifroest.commons.net.throttle.ThrottleControl;
import io.bifroest.commons.util.json.JSONUtils;

public class ServerThread<E extends Environment> extends Thread {
    private static final Logger log = LogManager.getLogger();

    public static final String CLIENT_TIMING = "commons.systems.net.client_timing.";

    private final E environment;
    private final String name;
    private final ThreadPoolExecutor threadPool;
    private ServerSocketHolder server;
    private final ThrottleControl throttle;
    private final int maximumPoolSize;
    private final BlockingQueue<Runnable> queue;
    private final IncomingConnectionHandlerFactory<E> connectionHandlerFactory;
    private final Optional<TaskID> monitorTask;
    private volatile boolean running;

    public ServerThread( E environment, JSONObject config, ThrottleControl throttle, IncomingConnectionHandlerFactory<E> connectionHandlerFactory ) throws IOException {
        this.environment = Objects.requireNonNull( environment );
        this.setDaemon( true );
        this.name = config.getString( "name" );
        setName( "Server-" + name );

        if ( config.getString( "type" ).equals( "tcp" ) ) {
            try {
                int port = config.getInt( "port" );
                log.info( "Create CommonServerSocket with port {}", Objects.toString( port ) );
                this.server = new CommonServerSocket( port );
            } catch( JSONException e ) {
                log.error( "Failed to read attribute 'port' from JSONconfigfile: ", e );
            }
        }
        this.maximumPoolSize = config.getInt( "poolsize" );
        ThreadFactory threads = new BasicThreadFactory.Builder().namingPattern( name + "-%d" ).build();
        this.queue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(
                1, 1, // thread count is set to the real initial value on the first run()
                0L, TimeUnit.MILLISECONDS,
                queue,
                threads
                );
        this.throttle = throttle;
        this.connectionHandlerFactory = connectionHandlerFactory;

        JSONObject monitor = config.optJSONObject( "monitor" );
        if ( monitor != null ) {
            long warnLimit = JSONUtils.getTime( "warnlimit", monitor, TIME_UNIT.SECOND );
            long frequency = JSONUtils.getTime( "frequency", monitor, TIME_UNIT.SECOND );
            monitorTask = Optional.of(
                    TaskRunner.runRepeated( new CommandMonitor( name, warnLimit ), "Command Monitor", Duration.ZERO, Duration.ofSeconds( frequency ), false ) );
        } else {
            monitorTask = Optional.empty();
        }

        SimpleProgramStateTracker.forContext( CLIENT_TIMING + name )
                .storingIn( "Server.ClientTiming." + name )
                .build();
    }

    private void adjustPoolSize() {
        int poolSize = (int)( throttle.getValue() * maximumPoolSize );
        if ( poolSize == 0 ) {
            poolSize = 1;
        }
        log.debug( "New pool size: {}", poolSize );

        threadPool.setCorePoolSize( poolSize );
        threadPool.setMaximumPoolSize( poolSize );
    }

    @Override
    public void run() {
        ThreadContext.put( "thread", Thread.currentThread().getName() );
        running = true;

        while ( running ) {
            try {
                log.debug( "Waiting for connection" );
                final ReadWriteableClientSocket socket = server.accept();
                log.debug( "Accepted incoming connection" );
                log.debug( "Size of queue: {} - just a snapshot!", queue.size() );
                adjustPoolSize();
                threadPool.submit( connectionHandlerFactory.create( environment, socket ) );
            } catch( IOException e ) {
                if ( !running ) {
                    if ( e instanceof SocketException && e.getMessage().equals( "Socket closed" ) ) {
                        // This exception is expected. The only way to interrupt
                        // (common) server.accept() is to externally close the socket.
                        log.debug( "Socket closed." );
                    }
                } else {
                    log.warn( "Exception while waiting on client connection:", e );
                }
            }
        }

        try {
            server.close();
        } catch( IOException e ) {
            log.warn( "Exception while closing socket", e );
        }
    }

    public void shutdown() {
        running = false;
        monitorTask.ifPresent( t -> TaskRunner.stopTask( t ) );
        log.info( "Shutting down ServerThread {}", name );
        try {
            server.close();
        } catch( IOException e ) {
            log.warn( "Exception while closing socket", e );
        }
        log.info( "Socket of {} closed", name );

        threadPool.shutdown();
        try {
            threadPool.awaitTermination( Long.MAX_VALUE, TimeUnit.DAYS );
        } catch( InterruptedException e ) {
            log.warn( "Interrupted while waiting for remaining thread", e );
        }
        log.info( "Threadpool of {} shut down", name );

        connectionHandlerFactory.shutdown();
        log.info( "Connection Handler of {} shut down", name );
    }
}
