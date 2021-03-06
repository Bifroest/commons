package io.bifroest.commons.net.throttle;

public final class LinearThrottleControl implements ThrottleControl {
    private final Sensor sensor;

    public LinearThrottleControl( Sensor sensor ) {
        this.sensor = sensor;
    }

    @Override
    public double getValue( ) {
        return sensor.getValue();
    }
}
