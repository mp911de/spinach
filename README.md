spinach - A scalable Java Disque client
======================================


[![Build Status](https://travis-ci.org/mp911de/spinach.svg)](https://travis-ci.org/mp911de/spinach) [![Coverage Status](https://coveralls.io/repos/mp911de/spinach/badge.svg?branch=master)](https://coveralls.io/r/mp911de/spinach?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/biz.paluch.redis/spinach/badge.svg)](https://maven-badges.herokuapp.com/maven-central/biz.paluch.redis/spinach)

Spinach is a scalable thread-safe Disque client providing both synchronous and
asynchronous connections. Multiple threads may share one connection. Spinach is based on
[lettuce](https://github.com/mp911de/lettuce).
Multiple connections are efficiently managed by the excellent netty NIO
framework.

* Works with Java 6, 7 and 8
* synchronous and asynchronous connections
* [Codecs](https://github.com/mp911de/lettuce/wiki/Codecs) (for UTF8/bit/JSON etc. representation of your data)


Maven Artifacts/Download
----------------

Currently there are no releases of spinach are available in the maven central repository. You can obtain
the library from https://oss.sonatype.org/content/repositories/snapshots/

```xml
<dependency>
  <groupId>biz.paluch.redis</groupId>
  <artifactId>spinach</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```


Basic Usage
-----------

```java
DisqueClient client = new DisqueClient("localhost")
DisqueConnection<String, String> connection = client.connect()
String jobId = connection.addjob("queue", "body", 1, SECONDS);
  
Job<String, String> job = connection.getjob("queue");
connection.ackjob(job.getId());
```

Each Disque command is implemented by one or more methods with names identical
to the lowercase Disque command name. Complex commands with multiple modifiers
that change the result type include the CamelCased modifier as part of the
command name.

Disque connections are designed to be long-lived, and if the connection is lost
will reconnect until close() is called. Pending commands that have not timed
out will be (re)sent after successful reconnection.

All connections inherit a default timeout from their DisqueClient and
and will throw a DisqueException when non-blocking commands fail to return a
result before the timeout expires. The timeout defaults to 60 seconds and
may be changed in the DisqueClient or for each individual connection.

Asynchronous Connections
------------------------

```java
DisqueConnection<String, String> async = client.connectAsync()
RedisFuture<String> jobId1 = async.addjob("queue", "body1", 1, SECONDS)
RedisFuture<String> jobId2 = async.addjob("queue", "body2", 1, SECONDS)

async.awaitAll(jobId1, jobId2) == true

jobId1.get() == "DI...SQ"
jobId2.get() == "DI...SQ"
 ```


Building
-----------

Spinach is built with Apache Maven. The tests require multiple running Disque instances for different test cases which
are configured using a ```Makefile```.

* Initial environment setup (clone and build `disque`): ```make prepare```
* Run the build: ```make test```
* Start Disque (manually): ```make start```
* Stop Disque (manually): ```make stop```

License
-------

* [Apache License 2.0] (http://www.apache.org/licenses/LICENSE-2.0)

Contributing
-------

Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in.
