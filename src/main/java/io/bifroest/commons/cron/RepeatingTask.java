package io.bifroest.commons.cron;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.MDC;

import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.cron.observer.RepeatingTaskObserver;

class RepeatingTask extends Thread implements StoppableTask {

    private static final Logger log = LogService.getLogger(RepeatingTask.class);

    private final String name;
    private final Runnable task;
    private final long initialDelayInMillis;
    private final long frequencyInMillis;
    private final RepeatingTaskObserver observer;

    private volatile boolean stop = false;
    private volatile boolean sleeping = false;

    public RepeatingTask( Runnable task, String name, long initialDelay, long frequency, TimeUnit unit, RepeatingTaskObserver observer ) {
        super( name );
        this.name = name;
        this.task = task;
        this.initialDelayInMillis = TimeUnit.MILLISECONDS.convert( initialDelay, unit );
        this.frequencyInMillis = TimeUnit.MILLISECONDS.convert( frequency, unit );
        this.observer = observer;
    }

    @Override
    public void run() {
        MDC.put( "thread", name );
        try {
            Thread.sleep( initialDelayInMillis );
        } catch ( InterruptedException e ) {
            // ignore
            // During shutdown, stop will be true, and the following loop will
            // be skipped
        }
        while ( !stop ) {
            long start = System.currentTimeMillis();
            observer.threadStarted( start );

            log.trace( "Executing {}", name );

            try {
                task.run();
            } catch ( Exception e ) {
                log.warn( "Task execution failed", e );
            }

            log.trace( "Done executing {}", name );

            observer.threadStopped( start );

            if ( !stop ) {
                try {
                    sleeping = true;
                    long sleeptime = frequencyInMillis - ( System.currentTimeMillis() - start );
                    if ( sleeptime >= 0 ) {
                        log.trace( "Thread for {} sleeping for {}ms", name, sleeptime );
                        Thread.sleep( sleeptime );
                    } else {
                        log.warn( "Thread for {} took {}ms too long - NOT sleeping", name, -sleeptime );
                    }
                } catch ( InterruptedException | IllegalArgumentException e ) {
                    // ignore
                } finally {
                    sleeping = false;
                }
            }
        }
    }

    @Override
    public void stopYourself() {
        stop = true;
        if ( sleeping ) {
            this.interrupt();
        }
    }

    @Override
    public String toString() {
        return "RepeatingTask " + getName();
    }
}
