Copycat
=======

## [User Manual](#user-manual)

Copycat is an extensible log-based distributed coordination framework for Java 8 built on the
[Raft consensus protocol](https://raftconsensus.github.io/).

### Overview

Copycat is a CP (consistent/partition-tolerant) oriented exercise in distributed coordination built on a consistent
replicated log. The core of Copycat is an extensible asynchronous framework that uses a mixture of
[gossip](http://en.wikipedia.org/wiki/Gossip_protocol) and the [Raft consensus protocol](https://raftconsensus.github.io/)
to provide a set of high level APIs that solve a variety of distributed systems problems including:
* [Leader election](#leader-elections)
* [Replicated state machines](#state-machines)
* [Strongly consistent state logs](#state-logs)
* [Eventually consistent event logs](#event-logs)
* [Distributed collections](#collections)
* [Resource partitioning](#resource-partitioning)
* [Failure detection](#failure-detection)
* [Remote execution](#remote-execution)

Copycat also provides integration with asynchronous networking frameworks like [Netty](http://netty.io) and
[Vert.x](http://vertx.io).

**Please note that Copycat is still undergoing heavy development, and until a beta release,
the API is subject to change.**

Copycat *will* be published to Maven Central once it is feature complete and well tested. Follow
the project for updates!

*Copycat requires Java 8. Though it has been requested, there are currently no imminent
plans for supporting Java 7.*

User Manual
===========

**Note: Some of this documentation may be inaccurate due to the rapid development currently
taking place on Copycat**

1. [Getting started](#getting-started)
   * [Adding Copycat as a Maven dependency](#adding-copycat-as-a-maven-dependency)
   * [Setting up the cluster](#setting-up-the-cluster)
   * [Configuring the protocol](#configuring-the-protocol)
   * [Configuring resources](#configuring-resources)
   * [Creating a Copycat instance](#creating-a-copycat-instance)
   * [Creating resources](#creating-resources)
1. [The Copycat dependency hierarchy](#the-copycat-dependency-hierarchy)
1. [State machines](#state-machines)
   * [Creating a state machine](#creating-a-state-machine)
   * [Creating a standalone state machine](#creating-a-state-machine-as-a-standalone-service)
   * [Configuring the state machine](#configuring-the-state-machine)
   * [Designing state machine states](#designing-state-machine-states)
   * [State machine commands](#state-machine-commands)
   * [State machine queries](#state-machine-queries)
   * [The state context](#the-state-context)
   * [Transitioning the state machine state](#transitioning-the-state-machine-state)
   * [Synchronous proxies](#synchronous-proxies)
   * [Asynchronous proxies](#asynchronous-proxies)
1. [Event logs](#event-logs)
   * [Creating an event log](#creating-an-event-log)
   * [Configuring the event log](#configuring-the-event-log)
   * [Writing events to the event log](#writing-events-to-the-event-log)
   * [Consuming events from the event log](#consuming-events-from-the-event-log)
   * [Replaying the log](#replaying-the-log)
1. [State logs](#state-logs)
   * [Creating a state log](#creating-a-state-log)
   * [Configuring the state log](#configuring-the-state-log)
   * [State commands](#state-commands)
   * [State queries](#state-queries)
   * [Submitting operations to the state log](#submitting-operations-to-the-state-log)
   * [Snapshotting](#snapshotting)
1. [Leader elections](#leader-elections)
   * [Creating a leader election](#creating-a-leader-election)
1. [Collections](#collections)
   * [AsyncMap](#asyncmap)
      * [Creating an AsyncMap](#creating-an-asyncmap)
      * [Configuring the AsyncMap](#configuring-the-asyncmap)
   * [AsyncList](#asynclist)
      * [Creating an AsyncList](#creating-an-asynclist)
      * [Configuring the AsyncList](#configuring-the-asynclist)
   * [AsyncSet](#asyncset)
      * [Creating an AsyncSet](#creating-an-asyncset)
      * [Configuring the AsyncSet](#configuring-the-asyncset)
   * [AsyncMultiMap](#asyncmultimap)
      * [Creating an AsyncMultiMap](#creating-an-asyncmultimap)
      * [Configuring the AsyncMultiMap](#configuring-the-asyncmultimap)
   * [AsyncLock](#asynclock)
      * [Creating an AsyncLock](#creating-an-asynclock)
      * [Configuring the AsyncLock](#configuring-the-asynclock)
1. [The Copycat cluster](#the-copycat-cluster)
   * [Cluster architecture](#cluster-architecture)
      * [Members](#members)
      * [Listeners](#listeners)
   * [Cluster configuration](#cluster-configuration)
   * [Leader election](#leader-election)
   * [Messaging](#messaging)
   * [Remote execution](#remote-execution)
1. [Protocols](#protocols)
   * [The local protocol](#the-local-protocol)
   * [Netty protocol](#netty-protocol)
   * [Vert.x protocol](#vertx-protocol)
   * [Vert.x 3 protocol](#vertx-3-protocol)
   * [Writing a custom protocol](#writing-a-custom-protocol)
1. [Architecture](#architecture)
   * [Strong consistency and Copycat's Raft consensus protocol](#strong-consistency-and-copycats-raft-consensus-protocol)
      * [Leader election](#leader-election-2)
      * [Command replication](#command-replication)
      * [Query consistency](#query-consistency)
      * [Log compaction](#log-compaction)
   * [Eventual consistency and Copycat's gossip protocol](#eventual-consistency-and-copycats-gossip-protocol)
      * [Log replication](#log-replication)
      * [Failure detection](#failure-detection)

## Getting started

### Adding Copycat as a Maven dependency
While Copycat provides many high level features for distributed coordination, the Copycat code base and architecture
is designed to allow each of the individual pieces of Copycat to be used independently of the others. However,
each Copycat component does have a common dependency on `copycat-core`.

Additionally, Copycat provides a single high-level `copycat-api` module which aggregates all the features provided
by Copycat. For first time users it is recommended that the `copycat-api` module be used.

```
<dependency>
  <groupId>net.kuujo.copycat</groupId>
  <artifactId>copycat-api</artifactId>
  <version>0.5.0-SNAPSHOT</version>
</dependency>
```

### Configuring the protocol

Copycat supports a pluggable protocol that allows developers to integrate a variety of frameworks into the Copycat
cluster. Each Copycat cluster must specific a `Protocol` in the cluster's `ClusterConfig`. For more information
see the section on [protocols](#protocols).

```java
ClusterConfig cluster = new ClusterConfig()
  .withProtocol(new NettyTcpProtocol());
```

Copycat core protocol implementations include [Netty](#netty-protocol), [Vert.x](#vertx), and [#Vert.x 3](vertx-3).
Each protocol implementation is separated into an independent module which can be added as a Maven dependency.

```
<dependency>
  <groupId>net.kuujo.copycat</groupId>
  <artifactId>copycat-netty</artifactId>
  <version>0.5.0-SNAPSHOT</version>
</dependency>
```

### Setting up the cluster

In order to connect your `Copycat` instance to the Copycat cluster, you must add a set of protocol-specific URIs to
the `ClusterConfig`. The cluster configuration specifies how to find the *seed* nodes - the core voting members of the
Copycat cluster - by defining a simple list of seed node URIs. For more about seed nodes see the section on
[cluster members](#members).

```java
ClusterConfig cluster = new ClusterConfig()
  .withProtocol(new NettyTcpProtocol())
  .withMembers("tcp://123.456.789.0", "tcp://123.456.789.1", "tcp://123.456.789.2");
```

Note that member URIs must be unique and must agree with the configured protocol instance. Member URIs will be checked
at configuration time for validity.

Because of the design of the [Raft algorithm](#strong-consistency-and-copycats-raft-consensus-protocol), it is strongly
recommended that any Copycat cluster have *at least three voting members*.

### Configuring resources

Each Copycat instance can support any number of various named resources. *Resource* is an abstract term for all of the
high level log-based data types provided by Copycat. Ultimately, each structure - whether is be an event log, state
machine, or collection - is backed by a Raft replicated log that is managed by the Copycat cluster coordinator
internally.

It's important to note that *all cluster resources must be configured prior to opening the Copycat instance.* This is
because Copycat needs a full view of the cluster's resources in order to properly configure and open various resources
internally. For instance, if a state log is created on node *a* in a three node cluster, nodes *b* and *c* also need
to create the same log in order to support replication.

Each of Copycat's resource types - `EventLog`, `StateLog`, `StateMachine`, etc - has an associated `Config` class.
This configuration class is used to configure various attributes of the resource, including Raft-specific configuration
options such as election timeouts and heartbeat intervals as well as resource specific configuration options. Each of
these configuration classes can then be added to the global `CopycatConfig` prior to startup.

```java
CopycatConfig config = new CopycatConfig()
  .withClusterConfig(new ClusterConfig()
    .withProtocol(new NettyTcpProtocol())
    .withMembers("tcp://123.456.789.0", "tcp://123.456.789.1", "tcp://123.456.789.2"));

config.addEventLogConfig("event-log", new EventLogConfig()
  .withSerializer(KryoSerializer.class)
  .withLog(new FileLog()
    .withSegmentSize(1024 * 1024)
    .withRetentionPolicy(new SizeBasedRetentionPolicy(1024 * 1024)));
```

The first argument to any `addResourceConfig` type method is always the unique resource name. Resource names are unique
across the entire cluster, not just for the specific resource type. So, if you attempt to overwrite an existing
resource configuration with a resource configuration of a different type, a `ConfigurationException` will occur.

The Copycat configuration API is designed to support arbitrary `Map` based configurations as well. Simply pass a map
with the proper configuration options for the given configuration type to the configuration object constructor:

```java
// Create an event log configuration map.
Map<String, Object> configMap = new HashMap<>();
configMap.put("serializer", "net.kuujo.copycat.util.serializer.KryoSerializer");

// Create a file-based log configuration map.
Map<String, Object> logConfigMap = new HashMap<>();
logConfigMap.put("class", "net.kuujo.copycat.log.FileLog");
logConfigMap.put("segment.size", 1024 * 1024);

// Create a log retention policy configuration map.
Map<String, Object> retentionConfigMap = new HashMap<>();
retentionConfigMap.put("class", "net.kuujo.copycat.log.SizeBasedRetentionPolicy");
retentionConfigMap.put("size", 1024 * 1024);

// Add the log retention policy to the log configuration map.
logConfigMap.put("retention-policy", retentionConfigMap);

// Add the log configuration map to the event log configuration.
configMap.put("log", logConfigMap);

// Construct the event log.
EventLogConfig config = new EventLogConfig(configMap);
```

### Creating a Copycat instance

To create a `Copycat` instance, call one of the overloaded `Copycat.create()` methods:
* `Copycat.create(String uri, ClusterConfig cluster)`
* `Copycat.create(String uri, CopycatConfig config)`

Note that the first argument to any `Copycat.create()` method is a `uri`. This is the protocol specific URI of the
*local* member, and it may or may not be a member defined in the provided `ClusterConfig`. This is because Copycat
actually supports eventually consistent replication for clusters much larger than the core Raft cluster - the cluster
of *seed* members defined in the cluster configuration.

```java
Copycat copycat = Copycat.create("tcp://123.456.789.3", cluster);
```

When a `Copycat` instance is constructed, a central replicated state machine is created for the entire Copycat cluster.
This state machine is responsible for maintaining the state of all cluster resources. In other words, it acts as a
central registry for other log based structures created within the cluster. This allows Copycat to coordinate the
creation, usage, and deletion of multiple log-based resources within the same cluster.

### Creating resources

With resources configured and the `Copycat` instance created, resources can be easily retrieved by calling any
of the resource-specific methods on the `Copycat` instance:
* `<T, U> CompletableFuture<EventLog<T, U>> eventLog(String name)`
* `<T, U> CompletableFuture<StateLog<T, U>> stateLog(String name)`
* `<T> CompletableFuture<StateMachine<T>> stateMachine(String name)`
* `CompletableFuture<LeaderElection> leaderElection(String name)`
* `<K, V> CompletableFuture<AsyncMap<K, V>> map(String name)`
* `<K, V> CompletableFuture<AsyncMultiMap<K, V>> multiMap(String name)`
* `<T> CompletableFuture<List<T>> list(String name)`
* `<T> CompletableFuture<Set<T>> set(String name)`
* `CompletableFuture<Lock> lock(String name)`

Note that resources are created asynchronously. This is because some resources may not already be running on the local
cluster. For instance, if the current node is not listed as one of the given resources' replicas, the resource will be
created and join the resource's replica cluster via a gossip protocol. For more information read about
[the Copycat cluster](#the-copycat-cluster)

## The Copycat dependency hierarchy
The Copycat project is organized into a number of modules based on specific use cases.

#### copycat-api
The `copycat-api` module is a high-level project that aggregates all the separate Copycat modules.

#### copycat-core
The `copycat-core` module is the core of Copycat which provides the base Raft implementation, configuration,
cluster management, messaging, logs, and protocol interfaces.

#### event-log
The `copycat-event-log` module is an event log implementation built on the Raft consensus protocol.
The event log supports both small strongly consistent event logs and large eventually consistent event
logs via a gossip protocol.

#### state-log
The `copycat-state-log` module is a strongly consistent, snapshottable log built on the Raft consensus protocol.

#### state-machine
The `copycat-state-machine` module provides a high-level state machine API on top of the `copycat-state-log` API.

#### leader-election
The `copycat-leader-election` module provides a simple Raft-based distributed leader election API.

#### collections
The `copycat-collections` module provides strongly- and eventually-consistent log-based distributed data
structures including `AsyncMap`, `AsyncMultiMap`, `AsyncList`, `AsyncSet`, and `AsyncLock`.

#### copycat-chronicle
The `copycat-chronicle` module is a fast [Chronicle Queue](https://github.com/OpenHFT/Chronicle-Queue) based log
implementation.

#### netty
The `copycat-netty` module provides a [Netty](http://netty.io) based protocol implementation.

#### vertx
The `copycat-vertx` module provides a [Vert.x 2](http://vertx.io) based protocol implementation.

#### vertx3
The `copycat-vertx3` module provides a [Vert.x 3](http://vertx.io) based protocol implementation.

## State machines

The most common use case for consensus algorithms is to support a strongly consistent replicated state machine.
Replicated state machines are similarly an essential feature of Copycat. Copycat provides a unique, proxy based
interface that allows users to operate on state machines either synchronously or asynchronously.

### Creating a state machine

To create a replicated state machine, you first must design an interface for the state machine states. In this case
we'll use one of Copycat's provided [collections](#collections) as an example.

States are used internally by Copycat to apply state machine commands and queries and transition between multiple
logical states. Each state within a state machine must implement a consistent explicitly defined interface. In this
case we're using the existing Java `Map` interface to define the state.

```java
public class DefaultMapState<K, V> implements Map<K, V> {
  private Map<K, V> map;

  @Initializer
  public void init(StateContext<AsyncMapState<K, V>> context) {
    map = context.get("value");
    if (map == null) {
      map = new HashMap<>();
      context.put("value", map);
    }
  }

  @Override
  public V put(K key, V value) {
    return map.put(key, value);
  }

  @Override
  public V get(K key) {
    return map.get(key);
  }

  @Override
  public V remove(K key) {
    return map.remove(key);
  }

  @Override
  public boolean containsKey(K key) {
    return map.containsKey(key);
  }

}
```

The `StateContext` can be used to store state values that Copycat will use to take snapshots of the state machine state
and compact the replicated log. For this reason, it is essential that state implementations store state in the
`StateContext`.

Once we've defined the map state, we can simply create a new map state machine via the `stateMachine` factory method.

```java
CopycatConfig config = new CopycatConfig()
  .withClusterConfig(cluster)
  .addStateMachineConfig("map", new StateMachineConfig()
    .withStateType(Map.class)
    .withInitialState(DefaultMapState.class));

Copycat copycat = Copycat.create("tcp://123.456.789.0", config);
copycat.open().get();

StateMachine<Map<String, String>> stateMachine = copycat.stateMachine("map").get();
stateMachine.open().get();
```

When a Copycat resource - such as a state machine, log, or election - is created, the resource must be opened before
it can be used. Practically all Copycat interfaces return `CompletableFuture` instances which can either be used to
block until the result is received or receive the result asynchronously. In this case we just block by calling the
`get()` method on the `CompletableFuture`.

### Creating a state machine as a standalone service

Copycat's architecture is designed such that individual resource types can be used completely independently of others.
The state machine module can be added as a separate Maven dependency, and state machines can be created independent
of the high level `Copycat` interface which aggregates all resource types into a single component.

To use the state machine as a standalone service, simply add the `copycat-state-machine` module as a Maven dependency:

```
<dependency>
  <groupId>net.kuujo.copycat</groupId>
  <artifactId>copycat-state-machine</artifactId>
  <version>0.5.0-SNAPSHOT</version>
</dependency>
```

The `StateMachine` interface provides its own constructors similar to the `Copycat.create` constructors.

```java
StateMachineConfig config = new StateMachineConfig()
  .withStateType(Map.class)
  .withInitialState(DefaultMapState.class)'

StateMachine<Map<K, V>> stateMachine = StateMachine.create("tcp://123.456.789.0", config);
stateMachine.open().get();
stateMachine.submit("get", "foo", "Hello world!").get();
```

### Configuring the state machine

State machines are configured using the `StateMachineConfig` class. This class contains vital configuration information
regarding how the state machine should handle startup and how it should apply commands.

As demonstrated in the example in the previous section, two vital components of the `StateMachineConfig` are the
*state type* and *initial state*. The state type is an *interface* which contains all of the state methods. All state
implementations must implement this interface. The initial state is the first state to which the state machine will
transition upon startup.

```java
StateMachineConfig config = new StateMachineConfig()
  .withStateType(Map.class)
  .withInitialState(DefaultMapState.class);
```

By default, all Copycat resources use the `KryoSerializer` for serialization. This should work fine for most use cases,
but if you so desire you can configure the `Serializer` class on the `StateMachineConfig`.

```java
StateMachineConfig config = new StateMachineConfig()
  .withStateType(Map.class)
  .withInitialState(DefaultMapState.class)
  .withSerializer(MyCustomSerializer.class);
```

### Designing state machine states

### State machine commands

State machine commands are annotated methods on the state machine state interface. Annotating a method with the
`@Command` annotation indicates that it is a stateful method call, meaning the operation that calls the method should
be persisted to the log and replicated. It is vital that *all* methods which alter the state machine's state be
identified by the `@Command` annotation. For this reason, all state machine methods are commands by default.

```java
public interface AsyncMap<K, V> {

  @Command
  V put(K key);

}
```

### State machine queries

Queries are the counter to commands. The `@Query` annotation is used to identify state machine methods which are
purely read-only methods. *You should never annotate a method that alters tha state machine state with the query
annotation.* If methods which alter the state machine state are not annotated as commands, calls to the method will
*not* be logged and replicated, and thus state will be inconsistent across replicas.

```java
public interface AsyncMap<K, V> {

  @Query
  V get(K key);

}
```

You can specify the required consistency of queries by defining the `@Query` annotation's `consistency` argument.
The query consistency allows you to control how read operations are performed. Copycat supports three consistency
levels:
* `NONE` - reads the state machine from the local node. This is the cheapest/fastest consistency level, but will often
  result in stale data being read
* `DEFAULT` - all reads go through the resource leader which performs consistency checks based on a lease period equal to the
  resource's heartbeat interval
* `FULL` - all reads go through the resource leader which performs a synchronous consistency check with a majority of
  the resource's replicas before applying the query to the state machine and returning the result. This is the most
  expensive consistency level.

Query consistency defaults to `DEFAULT`

```java
public interface AsyncMap<K, V> {

  @Query(consistency=Consistency.FULL)
  CompletableFuture<V> get(K key);

}
```

### The state context
Copycat's state machine provides a `StateContext` object in which state implementations should store state. The reason
for storing state in a separate object rather than in the states themselves is threefold. First, the `StateContext`
object persists throughout the lifetime of the state machine, even across states. Second, the state machine uses the
state held within the `StateContext` to take and install snapshots for log compaction. Snapshotting and log compaction
is performed automatically using the context's state; users need only store state in the context in order for snapshots
to work. Third, the state context provides a vehicle through which states can transition to other states.

To get the `StateContext` object for the state machine, simply add a method to your state annotated with the
`@Initializer` annotation.

```java
public class DefaultMap<K, V> implements Map<K, V> {
  private StateContext<AsyncMap<K, V>> context;

  @Initializer
  public void init(StateContext<AsyncMap<K, V>> context) {
    this.context = context;
  }

  public V get(K key) {
    return context.<Map<K, V>>get("the-map").get(key);
  }

}
```

### Transitioning the state machine state

To transition to another state via the `StateContext`, call the `transition` method. A perfect example of this is
a lock. This example transitions between two states - locked and unlocked - based on the current state.

```java
public class UnlockedLockState implements LockState {
  private StateContext<AsyncMap<K, V>> context;

  @Initializer
  public void init(StateContext<AsyncMap<K, V>> context) {
    this.context = context;
  }

  public void lock() {
    context.transition(new LockedLockState());
  }

  public void unlock() {
    throw new IllegalStateException("Lock is not locked");
  }

}
```

```java
public class LockedLockState implements LockState {
  private StateContext<AsyncMap<K, V>> context;

  @Initializer
  public void init(StateContext<AsyncMap<K, V>> context) {
    this.context = context;
  }

  public void lock() {
    throw new IllegalStateException("Lock is locked");
  }

  public void unlock() {
    context.transition(new UnlockedLockState());
  }

}
```

### Synchronous proxies

While Copycat's `StateMachine` interface exposes a `submit` method for submitting named commands and queries to the
state machine, it also provides a proxy based interface for operating on the state machine state interface directly.
To create a state machine proxy simply call the `createProxy` method on the `StateMachine` object, passing the proxy
interface to implement. The state machine supports both synchronous and asynchronous return values by checking whether
a given proxy interface method's return type is `CompletableFuture`.

```java
Map<String, String> proxy = stateMachine.createProxy(Map.class);
```

Once we've created a state machine proxy, we can simply call methods directly on the proxy object and Copycat will
internally submit [commands](#commands) and [queries](#queries) to the replicated state machine.

```java
proxy.put("foo", "Hello world!");
System.out.println(proxy.get("foo")); // Hello world!
```

### Asynchronous proxies

The state machine proxy feature also supports asynchronous proxies. It does this simply by checking the return type
of a given proxy method. If the proxy method's return type is `CompletableFuture` then the method will be executed
in a separate thread.

Often times asynchronous proxies require creating a separate proxy interface.

```java
public interface AsyncMap<K, V> {

  CompletableFuture<V> put(K key, V value);

  CompletableFuture<V> get(K key);

  CompletableFuture<V> remove(K key);

  CompletableFuture<Void> clear();

}
```

Aside from the return types, asynchronous proxies work the same way as synchronous proxies. To create an asynchronous
proxy simply call the `createProxy` method, passing the asynchronous proxy interface.

```java
AsyncMap<K, V> map = stateMachine.createProxy(AsyncMap.class);

map.put("foo", "Hello world!").thenRun(() -> {
  map.get("foo").thenAccept(value -> System.out.println(value)); // Hello world!
});
```

### Serialization

## Event logs

### Creating an event log

### Configuring the event log

### Writing events to the event log

### Consuming events from the event log

### Replaying the log

## State logs

### Creating a state log

### Configuring the state log

### State commands

### State queries

### Submitting operations to the state log

### Snapshotting

## Leader elections

### Creating a leader election

## Collections

### AsyncMap

#### Creating an AsyncMap

#### Configuring the AsyncMap

### AsyncList

#### Creating an AsyncList

#### Configuring the AsyncList

### AsyncSet

#### Creating an AsyncSet

#### Configuring the AsyncSet

### AsyncMultiMap

#### Creating an AsyncMultiMap

#### Configuring the AsyncMultiMap

### AsyncLock

#### Creating an AsyncLock

#### Configuring the AsyncLock

## The Copycat cluster

### Cluster architecture

#### Members

#### Listeners

### Cluster configuration

### Leader election

### Messaging

### Remote execution

## Protocols

### The local protocol

### Netty protocol

### Vert.x protocol

### Vert.x 3 protocol

### Writing a custom protocol

## Architecture

### Strong consistency and Copycat's Raft consensus protocol

#### Leader election

#### Command replication

#### Query consistency

#### Log compaction

### Eventual consistency and Copycat's gossip protocol

#### Log replication

#### Failure detection

### [User Manual](#user-manual)
