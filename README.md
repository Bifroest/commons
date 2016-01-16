# Profiling Commons

[![Build Status](https://travis-ci.org/Bifroest/commons.svg?branch=master)](https://travis-ci.org/Bifroest/commons)

[![Coverage Status](https://coveralls.io/repos/Bifroest/commons/badge.svg?branch=master&service=github)](https://coveralls.io/github/Bifroest/commons?branch=master)

## Systems

### Bootloader

The bootloader provides a generic main method, in com.goodgame.profiling.commons.GenericMain.
This main method needs two command line arguments, the first one is the class to use
as an environment, and the second one is the path to the config directory. The class
must have either a constructor accepting a java.nio.files.Path, though for compatibility
reasons, an optional InitD can be required. The generic main method will always pass null
as this parameter.

After this, you can configure the systems to boot and the generic main class will enable
these systems during startup. All of these systems must be available to a 
ServiceLoader\<SubSystem\<\>\> to make use of this system.

#### Snippet:

```
{
    "bootloader" : {
        "enabled-systems" : [ "systems.rmi-jmx", "systems.server.multi", "JvmProviderSystem" ]
    }
}
```

This will load the JMX system for debugging purposes, the multi server to interface
with the world and the JvmProviderSystem, because the multiserver needs it (currently,
this kind of dependency is not detected automatically just yet).


### Configuration System
The configuration system loads the configuration. from the initial configuration
path in the environment. Our configuration isn't just a single file, because we
want to support multiple teams and persons working on the same configuration. 

Thus, the configuration system walks the entire directory given and merges all
configuration files in the system. This merge is straightforward:

 - In order to merge 2 JSON Objects, we take all the keys existing in only one
   of the two objects and merge all keys contained in both JSON objects recursively.
 - JSONArrays are merged through concatenation.
 - Values cannot be merged. This is considered an error and will prevent
   an application start.

#### Configuration File Status Output (19.3.0+)

##### Snippet: 

```
{
    "configuration" : {
        "status-file" : "/var/lib/<service>/configuration_status.json"
    }
}
```

##### Effect:

If the status-file for the configuration is set, the configuration loader
will track the parse errors of each configuration file it loads and write
a status report into the configured file ("/var/lib/\<service\>/configuration\_status.json"
in this case). The file has the following format:

```
{
    "files" : [
        {
            "name" : "/etc/<service>/config_file_1",
            "parse_error" : "Missing closing } at line 42"
        },
        {
            "name" : "/etc/<service>/config_file_2",
        }
    ]
}
```

The file without a parse error was considered and loaded correctly. The file
which couldn't be parsed has it's parse error noted in this file.

This file is intended to be parsed, e.g. by a status page to notify
configuration suppliers about the state of their logs.

### Statistic System

The statistic system is responsible for collecting performance data about the service
and making this available to munin, graphite or other systems. Internally, it works
with an asynchronous event bus. Functional code usually just fires events on this bus
to notify the statistic system about interesting things happening in the code. Handlers
subscribe to events or superclasses of events in order to be informed about situations
in the code and in order to generate some kind of information.

All configuration of the statistic system is contained in a toplevel statistic json object.

#### Configuration of the event bus
The event bus is either a simple LinkedBlockingQueue with a single consumer if unconfigured,
or an LMAX Disruptor, if configured. For the disruptor, we can define the number of handler
threads and the size exponent for the ringbuffer. For example, our current bifroest uses the
following configuration:

```
{
    "statistics" : {
        "eventbus" : {
            "handler-count" : 1,
            "size-exponent" : 30
        }
    }
}
```
This has one handler-thread to execute statistic handlers and a ringbuffer with 2^30 elements.

#### Metric Pushes

Our core services push metrics without using diamond or other systems in between. This
greatly simplifies bootstrapping the system, since it's possible to just start a stream rewriter
with a metric push directly to the database. Once it's able to write "I'm not doing anything"
into the database, the stream rewriter is ready to go and further services can use it.

##### Composite Metric Push

The statistic system internally only uses a single metric push, however, we want metrics to 
end up in multiple places. For example, writing the metrics to a file is pretty nice for
quick debugging, but sending metrics to an actual storage backend with a graphite frontend
is better for long-term investigation.

This is implemented in the composite metric push. The composite metric push has multiple
inner metric pushes and delegates metrics to these sub-pushes. Individual failures are
logged and ignored, in order to get the metrics into as many places as possible.

In the configuration file, this looks as follows:

```
{
    "statistics" : {
        "metric-push" : {
            "type" : "composite",
            "inners" : [
                ... further metric pushes ...
            ]
        }
    }
}
```

#### Text File Push

### Bifroest

#### Tree Storage
The "treestorage" defines a folder in which Bifroest will save its Prefix Tree. The "nameRetention" parameter defines the maximum age of the leaves in the Tree. 
Every leaf of the tree which is older than the nameRetention will be removed during the next tree rebuild. The Blacklist parameter will be removed soon. It is not working anyway.

```
{
    "bifroest" : {
        "treestorage" : "test/data/tree",
        "nameRetention" : "5m",
        "blacklist" : []
    }
}
```

#### Retention Level
The Retention Configuration contains a list of all retention levels. Each level must contain a frequency, a block size,
and an amount of how many blocks of this level should exist. The frequency describes the interval of the recieved data, e.g: in level "seconds" there will be one new datapoint for each metric every 5 seconds.
"blockSize" is the size of the table in the database. As soon as the blockSize interval is over, there will be a new table created in the database. The amount of "blocks" in one level determines how long data will be stored
in its current form before it will be aggregated to the next level. The next level is optional. Patterns define accessLevels for a specific group
of metrics. The accessLevel is used to create database table names as well as it is used by the caching system. Requested metrics will be cached in their accessLevel cache, its next level and all following levels.

``` 
{
    "retention" : {
        "levels" : { 
            "seconds" : { 
                "frequency" : "5s",
                "blockSize" : "1h",
                "blocks" : 2,
                "next" : "minutes"
            }
            "minutes" : {
                "frequency" : "1m",
                "blockSize" : "2h",
                "blocks" : 6
            }
        },
        "patterns" : [
	    {
		"pattern": "(.*\\.)?Bifroest\\.(Bifroest|Aggregator|StreamRewriter)\\..*",
                "accessLevel": "seconds"
	    }
	]
    }
}
```

#### Caches
The Caching Configuration contains the definitions for all caching levels. There should be exactly one caching level for every retention level. In addition to that, the caching configuration contains the 
quality of service parameter as well as the parameters needed to save recently demanded metrics to files and preload them after a system reboot.

The higher the "quality-of-service" is set, the more likely graphs will display less exact data and read from a higher level cache than usually if there are datapoints containing the value null. If there are no
null-datapoints and the last reboot is not within the requested interval, the quality-of-service has no effect on the displayed data.
If a system shutdown is triggered, the eviction strategies will write the most requested metric names to a file. The amount of metric names written is a percentage of the visibleCacheSize. The metric files will 
have the same names as the accessLevels which belong to each eviction strategy and 
be saved in the "metric-storage" folder. 

On system startup Bifroest will search for the files and preload metrics. 
A higher amount of metrics to preload at system startup will lead to a longer startup time, but less preloaded metrics will make single requests during startup take a lot more time.

The caching configuration allows to configure the amount of database threads and metricCache threads. The database query threads are used for a single database query. The more database
query threads are used, the faster Bifroest receives the datapoints which it will write to the caches. MetricCache threads are writing a list of datapoints to a single cache. A higher number of those threads
will speed up the preloading process. A high amount of database threads and metricCache threads might speed up the starup process a lot, but it is likely that they won't be used during runtime anymore as long as
there will only be few new requests (= few database accesses). In case there will be a lot of uncached metric names requested during runtime it will be very helpful to have more threads to keep loading times 
as short as possible.

Every caching level contains a visible and total cachesize. The visible cache size must be smaller than the total cache size. The difference is a buffer. The cacheLineWidth parameter can be calculated using
the retention configuration, e.g: the level "seconds" has a frequency of 5s and block size of 1h -> 5s x 720 = 1h. Therefor the cacheLineWidth for the level "seconds" should be 720 datapoints.
VisibleCacheSize and TotalCacheSize must be configured for every access level, but will be calculated for all other levels during the creation of the caches.

```
{
    "cache" : {
        "quality-of-service" : 0.7,
        "percentage-of-metrics-to-save" : 0.5,
        "metric-storage" : "test/data/metricstorage",
        "number-of-database-threads" : 2,
        "number-of-metriccache-threads" 2,
        "levels" : {
            "seconds" : {
                "visibleCacheSize" : 100,
                "totalCacheSize" : 120,
                "cacheLineWidth" : 720
            },
            "minutes" : {
                "cacheLineWidth" : 120
            }
        }
    }
}
```

### Multi-Server
The Mulit-Server configuration is used to configure network interfaces. Every interface has a type, format, name, port, poolsize and accepts a specific type of command.
The network interfaces can be monitored. In order to do so the network interface needs a "monitor" parameter containing the warnlimit and frequency.
```
{
    "type" : "tcp",
    "format" : "json",
    "name" : "metric"
    "port" : 5101,
    "poolsize" : 5,
    "monitor" : {
        "warnlimit" : "3s",
        "frequency" : "5s"
    },
    "commands" : [ "get-sub-metrics" ]
}
```


