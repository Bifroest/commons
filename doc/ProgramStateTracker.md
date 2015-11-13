# Feature

SimpleProgramStateTracker and Direct Program State Tracker

a.k.a.

CPU Graphs like munin for Threadpools.

# Synopsis

## DirectProgramStateTracker

In your code:

```java
class FlorbMixer {
    private final DirectProgramStateTracker stateTracker;
    public SomeCommand()  {
        stateTracker = DirectProgramStateTracker.newTracker("florb-mixer");
    }

    public void doStuff() {
        tracker.startState("getting");
        getFlorbs();
        tracker.startState("mixing");
        mixFlorbs();
        tracker.stopState();
    }
}
```

## SimpleProgramStateTracker

In your statistic gatherer:

```java
@MetaInfServices
class FlorbStateTracker implements StatisticGatherer {
    @Override
    public void init() {
        SimpleProgramStateTracker.forContext("florb-mixer")
                                 .storingIn("florb-mixer")
                                 .build();
    }
}
```

In your code:
```java
class FlorbMixer {
    public void doStuff() {
        ProgramStateChanged.fireContextChangedToState( "florb-mixer", "getting" );
        getFlorbs();
        ProgramStateChanged.fireContextChangedToState( "florb-mixer", "mixing" );
        mixFlorbs();
        ProgramStateChanged.fireContextStopped();
    }
}
```

# Description

The ProgramStateTracker is a bit of arcane magic, but it allows deep insights
into the code. Conceptually, the program state tracker is comparable to 
normal profiling software, but with less performance impact due to less aggressive
instrumentation.

Consider our example, the FlorbMixer:

Assume we have one thread running doStuff() continuously. This is going to
create two metrics `florb-mixer.getting` and `florb-mixer.mixing`. The sum
of these two values is going to be 10^9, because one busy thread only executing
doStuff() spends 10^9 nanoseconds per second in doStuff (that's a fancy way
of saying "all the time").

The ratio of the two metrics depends on the relative runtimes of
getFlorbs() and mixFlorbs(). If getFlorbs() takes twice as long as mixFlorbs(),
`florb-mixer.getting` will be around 0.6\*10^9, while `florb-mixer.mixing` will
be around 0.3\*10^9, because the single thread spends 2/3 of it's execution time
in getFlorbs() and 1/3 of it's execution time in mixFlorbs(). This allows you
to decide if getFlorbs() or mixFlorbs() needs to be optimized. 

To add even more fun on top of that, this scales to multiple threads.
If we have 10 threads running doStuff() in parallel and busily, `florb-mixer.gettting`
and `florb-mixer.mixing` will add up to 10 \* 10^9, because we spend 10 concurrent
seconds per second in doStuff(). The ratio of the two metrics will tell you the 
relative cost of these two functions, even across the number of threads.

In addition, this also works if threads only spend a certain amount of time in
this function. As long as there is measurable time spent between two state changes,
the ProgramStateTracker metrics can visualize the relative runtimes.

## When to use which?

 - If you need to collect states across multiple classes and several parts of the
   code, use the `SimpleProgramStateTracker`. Using events in this manner
   will allow you to feed the tracker from all over the place without passing
   the tracker instance around. 

 - However, if you expect high load, lots of executions and many threads in your code,
   use the `DirectProgramStateTracker`. Otherwise, the amount of events will
   overload the statistic system and your service will break down.


## How to I use this in your frontend?

 - Scale everything with 1e-9. This gives you seconds
 - Stack the values. This gives you a pretty accurate feeling about how many
   threads are currently busy at a given time.
 - Ensure that missing values are set to 0.
 - Do not fear wildcards. The visualization can handle threads across multiple
   instances of the same application.
