package io.bifroest.commons.statistics.gathering;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.kohsuke.MetaInfServices;

import io.bifroest.commons.statistics.WriteToStorageEvent;
import io.bifroest.commons.statistics.eventbus.EventBusManager;
import io.bifroest.commons.statistics.storage.MetricStorage;

@MetaInfServices
public class JVMStatisticGatherer implements StatisticGatherer {
    @Override
    public void init() {
        EventBusManager.createRegistrationPoint().sub( WriteToStorageEvent.class, event -> {
            MetricStorage jvm = event.storageToWriteTo().getSubStorageCalled( "jvm" );
            Runtime runtime = Runtime.getRuntime();
            List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            
            jvm.store( "mem.total", runtime.totalMemory() );
            jvm.store( "mem.max", runtime.maxMemory() );
            jvm.store( "mem.free", runtime.freeMemory() );
            jvm.store( "mem.used", runtime.totalMemory() - runtime.freeMemory() );
            jvm.store( "mem.usedFraction", ( runtime.totalMemory() - runtime.freeMemory() ) / (double) runtime.maxMemory() );
            for( GarbageCollectorMXBean bean : gcMXBeans ) {
                jvm.store( "gc." + bean.getName() + ".count", bean.getCollectionCount());
                jvm.store( "gc." + bean.getName() + ".time", bean.getCollectionTime());
            }
            jvm.store( "uptimeMillis", ManagementFactory.getRuntimeMXBean().getUptime() );
        });
    }
}
