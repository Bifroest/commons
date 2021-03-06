package io.bifroest.commons.statistics.cache;

import java.util.concurrent.atomic.LongAdder;

import io.bifroest.commons.statistics.WriteToStorageEvent;
import io.bifroest.commons.statistics.eventbus.EventBusManager;
import io.bifroest.commons.statistics.storage.MetricStorage;

public final class CacheTracker {
    private LongAdder dodges = new LongAdder();
    private LongAdder misses = new LongAdder();
    private LongAdder hits = new LongAdder();
    private LongAdder partialHits = new LongAdder();
    private LongAdder evictions = new LongAdder();

    private volatile int size;
    private volatile int visibleSize;

    private CacheTracker() {
    }

    public void cacheDodge( int size, int visibleSize ) {
        dodges.increment();
        this.size = size;
        this.visibleSize = visibleSize;
    }

    public void cacheMiss( int size, int visibleSize ) {
        misses.increment();
        this.size = size;
        this.visibleSize = visibleSize;
    }

    public void cacheHit( int size, int visibleSize ) {
        hits.increment();
        this.size = size;
        this.visibleSize = visibleSize;
    }

    public void cachePartialHit( int size, int visibleSize ) {
        partialHits.increment();
        this.size = size;
        this.visibleSize = visibleSize;
    }

    public void cacheEviction() {
        evictions.increment();
    }
    
    public void update(int size, int visibleSize){
    	this.size = size;
    	this.visibleSize = visibleSize;
    }


    private void writeTo( MetricStorage storage, String[] nameParts ) {
        for ( String np : nameParts ) storage = storage.getSubStorageCalled( np );
        storage.store( "Dodges", dodges.doubleValue() );
        storage.store( "Misses", misses.doubleValue() );
        storage.store( "Hits", hits.doubleValue() );
        storage.store( "PartialHIts", partialHits.doubleValue() );
        storage.store( "Accesses", dodges.doubleValue() + misses.doubleValue() + hits.doubleValue() + partialHits.doubleValue() );
        storage.store( "Evictions", evictions.doubleValue() );
        storage.store( "Size", size );
        storage.store( "MaxSize", visibleSize );
    }

    public static CacheTracker storingIn( String... nameParts ) {
        CacheTracker r = new CacheTracker();
        EventBusManager.createRegistrationPoint().sub( WriteToStorageEvent.class, e -> {
            r.writeTo( e.storageToWriteTo(), nameParts );
        });
        return r;
    }
}
