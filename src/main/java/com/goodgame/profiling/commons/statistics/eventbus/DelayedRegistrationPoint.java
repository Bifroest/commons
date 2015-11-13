package com.goodgame.profiling.commons.statistics.eventbus;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class DelayedRegistrationPoint implements EventBusRegistrationPoint {

    private final List<Pair<Class<Object>, EventBusSubscriber<Object>>> subscribers = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <EVENT_CLASS> void subscribe( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber ) {
        subscribers.add(Pair.of((Class<Object>)event, (EventBusSubscriber<Object>) subscriber));
    }

    public void addSubscribersTo(EventBus bus) {
        EventBusRegistrationPoint realRegPoint = bus.createRegistrationPoint();
        subscribers.forEach(p -> realRegPoint.sub(p.getLeft(), p.getRight()));
    }

    @Override
    public String toString() {
        return "DelayedRegistrationPoint{" + "subscribers=" + subscribers + '}';
    }
}
