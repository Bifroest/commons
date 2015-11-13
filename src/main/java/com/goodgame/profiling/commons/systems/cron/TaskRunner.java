package com.goodgame.profiling.commons.systems.cron;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.goodgame.profiling.commons.logging.LogService;
import com.goodgame.profiling.commons.systems.cron.observer.DummyRepeatingTaskObserver;
import com.goodgame.profiling.commons.systems.cron.observer.WatchdogRepeatingTaskObserver;

public final class TaskRunner {
    private static final Logger log = LogService.getLogger(TaskRunner.class);

    private TaskRunner() {
        // static class
    }

    private static void runOnce( Runnable task, String name, long initialDelay, TimeUnit unit ) {
        OneTimeTask thread = new OneTimeTask( task, name, initialDelay, unit );
        thread.start();
    }

    public static void runOnce( Runnable task, String name, Duration initialDelay ) {
        runOnce( task, name, initialDelay.toNanos(), TimeUnit.NANOSECONDS );
    }

    private static TaskID runRepeated( Runnable task, String name, long initialDelay, long frequency, TimeUnit unit, boolean watchdog ) {
        log.info( "Adding repeating task: {}", name );

        RepeatingTask thread;
        if ( watchdog ) {
            thread = new RepeatingTask( task, name, initialDelay, frequency, unit,
                    new WatchdogRepeatingTaskObserver( name + "-repeating-task-watchdog", TimeUnit.MILLISECONDS.convert( frequency, unit ) ) );
        } else {
            thread = new RepeatingTask( task, name, initialDelay, frequency, unit, new DummyRepeatingTaskObserver() );
        }
        thread.start();
        return new TaskID( thread );
    }

    public static TaskID runRepeated( Runnable task, String name, Duration initialDelay, Duration frequency, boolean watchdog ) {
        return runRepeated( task, name, initialDelay.toNanos(), frequency.toNanos(), TimeUnit.NANOSECONDS, watchdog );
    }

    public static void stopTask( TaskID taskId ) {
        StoppableTask task = taskId.task;

        log.info( "Telling task {} to shut down", task.toString() );

        task.stopYourself();
        try {
            log.info( "Joining task {}", task.toString() );
            task.join();
        } catch( InterruptedException e ) {
            log.warn( "Interrupted while joining thread", e );
        }
    }

    public static class TaskID {
        private final StoppableTask task;

        private TaskID( StoppableTask task ) {
            this.task = task;
        }
    }
}
