
package io.bifroest.commons.statistics;

import io.bifroest.commons.statistics.WriteToStorageEvent;
import static io.bifroest.commons.collections.versioned_strings.MetricMatcher.containsMetric;
import static io.bifroest.commons.statistics.eventbus.EventBusManager.EventBusForce.VROOM;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;

import io.bifroest.commons.model.Metric;
import io.bifroest.commons.statistics.eventbus.EventBusImpl;
import io.bifroest.commons.statistics.eventbus.EventBusManager;
import io.bifroest.commons.statistics.storage.TrieMetricStorage;

final class EventAssert {
    private Collection<Metric> metrics = null;
    
    private EventAssert() {}

    public static EventAssert whenISetup(Runnable setup) {
        EventBusManager.setEventBus(new EventBusImpl(), VROOM);
        EventBusManager.actuallyRegisterHandlers();
        setup.run();
        return new EventAssert();
    }

    public EventAssert andISetup(Runnable setup) {
        setup.run();
        return this;
    }

    public EventAssert andIFire(Object o) {
        EventBusManager.synchronousFire( o );
        return this;
    }

    private void collectTheMetrics( Instant when ) {
        TrieMetricStorage storage = new TrieMetricStorage();
        EventBusManager.synchronousFire(
            new WriteToStorageEvent(
            Clock.fixed( when, ZoneId.systemDefault() ),
            storage));
        this.metrics = storage.getAll();
    }

    public void thenIExpectTheMetric(String name, double value) {
        if( metrics == null) {
            throw new IllegalStateException( "No metrics collected" );
        }
        assertThat(this.metrics, containsMetric(name, value));
    }

    public EventAssert andTheMetric(String name, double value) {
        if( metrics == null) {
            throw new IllegalStateException( "No metrics collected" );
        }
        assertThat(this.metrics, containsMetric(name, value));
        return this;
    }

    public EventAssert andICollectMetricsAt(Instant when) {
        collectTheMetrics(when);
        return this;
    }

    public EventAssert andICollectMetricsAgainAt(Instant when) {
        collectTheMetrics(when);
        return this;
    }
}
