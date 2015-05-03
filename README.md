lettuce - A scalable Java Redis client
======================================


[![Build Status](https://travis-ci.org/mp911de/spinach.svg)](https://travis-ci.org/mp911de/spinach) [![Coverage Status](https://img.shields.io/coveralls/mp911de/spinach.svg)](https://coveralls.io/r/mp911de/spinach) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/biz.paluch.redis/spinach/badge.svg)](https://maven-badges.herokuapp.com/maven-central/biz.paluch.redis/spinach)

Spinach is a scalable thread-safe Disque client providing both synchronous and
asynchronous connections. Multiple threads may share one connection. Spinach is based on
[lettuce](https://github.com/mp911de/lettuce).
Multiple connections are efficiently managed by the excellent netty NIO
framework.

This version of lettuce has been tested against Disque.

* Works with Java 6, 7 and 8
* synchronous and [asynchronous connections](https://github.com/mp911de/lettuce/wiki/Asynchronous-Connections)
* [CDI](https://github.com/mp911de/lettuce/wiki/CDI-Support) and [Spring](https://github.com/mp911de/lettuce/wiki/Spring-Support) support
* [Codecs](https://github.com/mp911de/lettuce/wiki/Codecs) (for UTF8/bit/JSON etc. representation of your data)


Maven Artifacts/Download
----------------

Releases of spinach are available in the maven central repository.

```xml
<dependency>
  <groupId>biz.paluch.redis</groupId>
  <artifactId>spinach</artifactId>
  <version>0.1</version>
</dependency>
```

or snapshots at https://oss.sonatype.org/content/repositories/snapshots/

Basic Usage
-----------

```java
  DisqueClient client = new DisqueClient("localhost")
  DisqueConnection<String> connection = client.connect()
  String value = connection.get("key")
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
RedisFuture<String> set = async.set("key", "value")
RedisFuture<String> get = async.get("key")

async.awaitAll(set, get) == true

set.get() == "OK"
get.get() == "value"
 ```


Performance
-----------

Spinach is made for performance. The async API can issue about 300Kops/sec whereas the sync API performs about 13Kops/sec (GET's, PING's).
Sync performance depends on the Redis performance (tested on a Mac, 2,7 GHz Intel Core i7).

Building
-----------

Spinach is built with Apache Maven. The tests require multiple running Disque instances for different test cases which
are configured using a ```Makefile```.

* Initial environment setup (clone and build `redis`): ```make prepare```
* Run the build: ```make test```
* Start Redis (manually): ```make start```
* Stop Redis (manually): ```make stop```

License
-------

* [Apache License 2.0] (http://www.apache.org/licenses/LICENSE-2.0)

Contributing
-------

Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in.
