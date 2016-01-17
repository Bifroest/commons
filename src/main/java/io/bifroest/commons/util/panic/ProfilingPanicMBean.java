package io.bifroest.commons.util.panic;

import java.time.Duration;

public interface ProfilingPanicMBean {
    public void panic();
    
    public void dontPanic(Duration relaxTime);
}
