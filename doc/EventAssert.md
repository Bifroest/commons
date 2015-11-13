# Feature

EventAssert - Unit testing event based statistic gatherers

# Synopsis

```java
whenISetup(() -> SimpleProgramStateTracker.forContext( "someContext" )
                 .storingIn( "s1.s2" )
                 .build() )
    .andIFire(new ProgramStateChanged( "someContext",
                                       Optional.of("writing-documentation"),
                                       1,
                                       Instant.ofEpochMilli( 0 )))
    .andIFire(new ProgramStateChanged( "someContext",
                                       Optional.empty(),
                                       1,
                                       Instant.ofEpochMilli( 1000 )))
    .thenIExpectTheMetric("s1.s2.writing-documentation", 1e9);
```


# Description

EventAssert is made to make testing the event driven statistic gatherers easier.
It offers a fluent API. Its structure is inspired by behaviour driven testing.

The first method you need to call is `whenISetup(Runnable)`. This method
will *forcefully reset the EventBus* in order to get a clean slate - don't be
surprised by that. After that, the runnable you pass in is executed, use this
to create your statistic gatherer under test.

After this, you should have multiple calls to `andIFire(Object)`. This method
will fire the events on the currently set eventbus. 

Finally, you'll want to expect metrics with `thenIExpectTheMetric` and 
`andTheMetric`. Both methods get a metric name and an expected value. If no
metrics are currently collected, both of these methods will collect metrics
with a WriteToStorageEvent. In other words,
`thenIExpectTheMetric(...).andTheMetric(...)` fires one WriteToStorage event
and asserts on the set of collected metrics twice. 

If you need to collect the metric sets multiple times, EventAssert offers
the methods `andICollectTheMetrics()` and `andICollectTheMetricsAgain()`. Both
of these fire a WriteToStorage event unconditionally.
