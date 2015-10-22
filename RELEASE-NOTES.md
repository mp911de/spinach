              spinach 0.3 RELEASE NOTES


SocketAddressSupplier API
-------------------------

The SocketAddressSupplier API allows to control the used
connection points/SocketAddresses for initial connection and reconnection.
The use case for controlling the source and sequence of
connection points are failover and cluster node discovery.

The API exposes a factory for `SocketAddressSupplier`s which accepts
a `RedisURI`. By default, multiple addresses are utilized in with RoundRobin.

The predefined methods can be found in the `SocketAddressSupplierFactory.Factories` enum:

* `ROUND_ROBIN`: Cyclic use of multiple addresses specified by the `RedisURI`
* `HELLO_CLUSTER`: Uses `ROUND_ROBIN` for the initial connect. Once a connection is
   established the mechanism obtains the cluster nodes using the `HELLO`
   command *AT STARTUP*. Periodical scheduling/updating is currently not implemented.
   This can be however achieved by implementing an own factory
   that calls the `reloadNodes()` method periodically/on demand.

New mechanisms can be implemented by implementing the
`SocketAddressSupplier`/`SocketAddressSupplierFactory` interfaces.

Submit your mechanism by opening a Pull-Request if you want to contribute to spinach.


```
DisqueClient client = new DisqueClient();

DisqueConnection<String, String> connection = client.connect(new Utf8StringCodec(), disqueURI,
                SocketAddressSupplierFactory.Factories.ROUND_ROBIN);


DisqueConnection<String, String> connection = client.connect(new Utf8StringCodec(), disqueURI,
                SocketAddressSupplierFactory.Factories.HELLO_CLUSTER);
```

QueueListener API
-----------------

Spinach allows a push pattern to obtain jobs from Disque. The QueueListener API
introduces a listener/notification style by utilizing rx-java Observables.
The QueueListener API exposes an `Observable<Job>` that emits jobs.
The jobs can be processed asynchronously by applying transformations
or `doOnNext` steps. Each `Observable<Job>` allocates at the time
of subscription resources. The resources are released when unsubscribing
from the subscription.

```
QueueListenerFactory<String, String> queueListenerFactory = QueueListenerFactory.create(disqueURI,
            new Utf8StringCodec(), "queue");  // 1

Observable<Job<String, String>> getjobs = queueListenerFactory.getjobs(); // 2

Subscription subscribe = getjobs.doOnNext(new Action1<Job<String, String>>() { // 3
            @Override
            public void call(Job<String, String> job) {
                // process the job
                System.out.println(job.getId());
            }
        }).doOnNext(new Action1<Job<String, String>>() {
            @Override
            public void call(Job<String, String> job) {
                // ack the job (different connection)
                connection.sync().ackjob(job.getId());
            }
        }).subscribe(); // 4
```

1. The `QueueListenerFactory` is set up. You can reuse an existing `DisqueClient`
   to reuse thread pools.
2. Create an `Observable<Job>` by calling the `getjobs` method. This call
   just creates the observable, but no resources are allocated.
3. Apply transformations or set up callbacks to process the jobs
4. Finally subscribe and open the connection/listener process

To be honest, the QueueListener API is just a side-product of the
recommendation to keep track of the producer node id when receiving jobs.
A client can optimize locality to connect directly the node that produces
most of the received jobs. The QueueListenerFactory can initiate locality tracking
for a particular observable and can enable periodically scheduled checks to
switch to the node that produced the most received jobs.

The node switch can also be triggered directly on a QueueListenerFactory for all
active listeners with enabled locality tracking.

```
QueueListenerFactory<String, String> queueListenerFactory = QueueListenerFactory.create(disqueURI,
            new Utf8StringCodec(), "queue");  // 1

Observable<Job<String, String>> getjobs = queueListenerFactory
                                            .withLocalityTracking() // 2
                                            .withNodeSwitching(2, TimeUnit.MINUTES) // 3
                                            .getjobs(); // 4

Subscription subscribe = getjobs.doOnNext(new Action1<Job<String, String>>() { // 5
            @Override
            public void call(Job<String, String> job) {
                // process the job
                System.out.println(job.getId());
            }
        }).doOnNext(new Action1<Job<String, String>>() {
            @Override
            public void call(Job<String, String> job) {
                // ack the job (different connection)
                connection.sync().ackjob(job.getId());
            }
        }).subscribe(); // 6

queueListenerFactory.switchNodes(); // 7
```

1. The `QueueListenerFactory` is set up. You can reuse an existing `DisqueClient`
   to reuse thread pools.
2. Enable locality tracking for this particular `Observable<Job>`
3. Enable node switching. The check whether node switching is necessary and
   the actual reconnect happens every 2 minutes.
4. Create an `Observable<Job>` by calling the `getjobs` method. This call
   just creates the observable, but no resources are allocated.
5. Apply transformations or set up callbacks to process the jobs
6. Finally subscribe and open the connection/listener process
7. Initiate a programmatic node switch check

The QueueListener API does not emit a terminal event.

Please note this API is experimental.


Enhancements
------------
* Support a pluggable reconnect mechanism #9
* Keep track of nodeId's that were obtained by GETJOB #8

Other Changes
-------------

spinach requires a minimum of Java 8 to build and Java 6 run. It is tested continuously against Disque unstable.

For complete information on spinach see the website:

* http://github.com/mp911de/spinach
* http://github.com/mp911de/spinach/wiki
* http://spinach.paluch.biz/

