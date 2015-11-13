package com.goodgame.profiling.commons.statistics.eventbus.disruptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBus;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusRegistrationPoint;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public final class DisruptorEventBus implements EventBus {
    private static final Logger log = LogManager.getLogger();
    
    private List<StatisticEventHandler> handlers = new ArrayList<StatisticEventHandler>();
    private int next = 0;
    private Disruptor<StatisticEventHolder> disruptor;
    private LongAdder eventCount = new LongAdder();
    private ExecutorService executor;

    @SuppressWarnings( "unchecked" )
    public DisruptorEventBus( int handlerCount, int bufferSizeShift ) {
        executor = Executors.newFixedThreadPool( handlerCount,
                                                          r -> new Thread(r, "DisruptorHandler") );
        EventHolderFactory factory = new EventHolderFactory();

        int bufferSize = 1 << bufferSizeShift;
        disruptor = new Disruptor<>( factory, bufferSize, executor );

        for ( int  i = 0; i < handlerCount; i ++ ) {
            handlers.add( new StatisticEventHandler( i ) );
            disruptor.handleEventsWith( handlers.get( handlers.size() - 1 ) );
        }

        disruptor.start();

        this.createRegistrationPoint().sub( WriteToStorageEvent.class, e -> {
            MetricStorage workStorage = e.storageToWriteTo().getSubStorageCalled( "EventBus" );
            workStorage.store( "EventsFired", eventCount.doubleValue() );
        });
    }

    @Override
    public void fire( final Object event ) {
        eventCount.increment();
        RingBuffer<StatisticEventHolder> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            StatisticEventHolder holder = ringBuffer.get( sequence );
            holder.set( event );
            holder.setLatch( null );
        } finally {
            ringBuffer.publish( sequence );
        }
    }

    @Override
    public void synchronousFire( final Object event ) {
        eventCount.increment();
        CountDownLatch latch = new CountDownLatch( handlers.size() );
        try {
            RingBuffer<StatisticEventHolder> ringBuffer = disruptor.getRingBuffer();
            long sequence = ringBuffer.next();
            try {
                StatisticEventHolder holder = ringBuffer.get( sequence );
                holder.set( event );
                holder.setLatch( latch );
            } finally {
                ringBuffer.publish( sequence );
            }
            latch.await();
        } catch ( InterruptedException e ) {
            return;
        }
    }

    @Override
    public void shutdown() throws InterruptedException {
        disruptor.halt();
        executor.shutdown();
    }

    @Override
    public EventBusRegistrationPoint createRegistrationPoint() {
        EventBusRegistrationPoint registrationPoint = handlers.get( next );
        next = ( next + 1 ) % handlers.size();
        return registrationPoint;
    }

    @Override
    public void describeSubscribersInLog( String outerIndent ) {
        log.info( outerIndent + "Eventbus: " + this.getClass().getSimpleName() );
        handlers.forEach( h -> h.describeSubscribersInLog( outerIndent + "\t" ) );
    }
}
