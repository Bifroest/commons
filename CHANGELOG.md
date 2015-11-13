
# 26.9.1

## New Features

### Delayed Registration points

Before the statistic system boots, the event bus manager returns delayed 
registration points. These registration points don't actually register
you subscribers on the bus just yet - that happens once the statistic system
tells the EventBusManager to actually perform registrations.

In short, EventBusManager.createRegistrationPoint().subscribe() will 
always do the right thing now.

### EventbusRegistrationPoint.subscribe can be chained.

The subscribe method now chains, so you can do:

```
EventBusManager.getRegistrationPoint()
               .subscribe(MyEvent, e -> ...)
               .subscribe(WriteToStorageEvent, e -> ...);
```

This makes it easier to write fine-grained statistic gatherers.

### EventAssert

There is a new test method to test statistic gathers in a BDD like fashion:

```
whenISetup( () -> createStatisticGatherer() )
  .andIFire( new SomeEvent() )
  .thenIExpectTheMetric( "a.b", 42);
```

More details are in doc/EventAssert.md

## Split of SimpleProgramStateTracker

The simple state tracker has been split into the SimpleProgramStateTracker and the
DirectProgramStateTracker. The SimpleProgramStateTracker is being controlled with
events, while the DirectProgramStateTracker is controlled with method calls. This
is useful in high-volume situations, if the SimpleProgramStateTracker would
overload the event bus.
