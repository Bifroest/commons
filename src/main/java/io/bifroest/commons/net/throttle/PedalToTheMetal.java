package io.bifroest.commons.net.throttle;

public class PedalToTheMetal implements ThrottleControl {
    @Override
    public double getValue() {
        return 1;
    }
}
