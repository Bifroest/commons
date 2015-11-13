package com.goodgame.profiling.commons.statistics.eventbus;

import static com.goodgame.profiling.commons.statistics.eventbus.EventBusManager.EventBusForce.VROOM;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class EventBusManagerTest {
    @Test
    public void actuallyRegistersDelayedRegistrations() {
        EventBus bus = mock(EventBus.class);
        EventBusManager.setEventBus( bus, VROOM );

        EventBusSubscriber<Object> handler = e -> {};

        EventBusManager.createRegistrationPoint()
                       .sub( Object.class, handler );
        
        verify(bus, never()).createRegistrationPoint();

        EventBusRegistrationPoint rp = mock(EventBusRegistrationPoint.class);
        when(bus.createRegistrationPoint()).thenReturn( rp );

        EventBusManager.actuallyRegisterHandlers();

        verify(rp, times(1)).sub(Object.class, handler);
    }

    @Test
    public void preservesRegistrationPointMapping() {
        EventBus bus = mock(EventBus.class);
        EventBusManager.setEventBus( bus, VROOM );

        EventBusSubscriber<Object> handler11 = e -> {};
        EventBusSubscriber<Object> handler12 = e -> {};
        EventBusSubscriber<Object> handler13 = e -> {};
        EventBusSubscriber<Object> handler21 = e -> {};

        EventBusManager.createRegistrationPoint()
                       .sub(Object.class, handler11)
                       .sub(Object.class, handler12)
                       .sub(Object.class, handler13);

        EventBusManager.createRegistrationPoint()
                       .sub(Object.class, handler21);

        verify(bus, never()).createRegistrationPoint();

        EventBusRegistrationPoint rp1 = mock(EventBusRegistrationPoint.class);
        EventBusRegistrationPoint rp2 = mock(EventBusRegistrationPoint.class);
        
        when(bus.createRegistrationPoint()).thenReturn( rp1, rp2 );

        EventBusManager.actuallyRegisterHandlers();

        verify(rp1, times(1)).sub(Object.class, handler11);
        verify(rp1, times(1)).sub(Object.class, handler12);
        verify(rp1, times(1)).sub(Object.class, handler13);
        verify(rp1, never()).sub(Object.class, handler21);

        verify(rp2, never()).sub(Object.class, handler11);
        verify(rp2, never()).sub(Object.class, handler12);
        verify(rp2, never()).sub(Object.class, handler13);
        verify(rp2, times(1)).sub(Object.class, handler21);
    }

    @Test
    public void passesLateRegistrationsImmediately() {
        EventBus bus = new EventBusImpl();
        EventBusManager.setEventBus( bus, VROOM );

        EventBusManager.actuallyRegisterHandlers();


        @SuppressWarnings("unchecked")
        EventBusSubscriber<Object> handler = mock(EventBusSubscriber.class);

        EventBusRegistrationPoint rp = EventBusManager.createRegistrationPoint();
        rp.sub( Object.class, handler );
        Object testEvent = "rabbit";
        EventBusManager.synchronousFire( testEvent );
        verify(handler, times(1)).onEvent(testEvent);
    }

    @Test
    public void hasIdempotentActuallyRegisterHandlers() {
        EventBus bus = mock(EventBus.class);
        EventBusManager.setEventBus( bus, VROOM );
        EventBusSubscriber<Object> handler = e -> {};
        EventBusRegistrationPoint rp = mock(EventBusRegistrationPoint.class);
        when(bus.createRegistrationPoint()).thenReturn( rp );

        EventBusManager.createRegistrationPoint()
                       .sub( Object.class, handler );
        EventBusManager.actuallyRegisterHandlers();
        EventBusManager.actuallyRegisterHandlers();
        verify(rp, times(1)).sub( Object.class, handler );
    }
}
