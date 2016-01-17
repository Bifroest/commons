package io.bifroest.commons.systems.net.throttle;

public interface ThrottleControl {
    /* Also returns a value between 0 and 1 (both inclusive) */
    double getValue( );
}
