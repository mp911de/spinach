spinach - A scalable Java Disque client
======================================


[![Build Status](https://travis-ci.org/mp911de/spinach.svg)](https://travis-ci.org/mp911de/spinach) [![Coverage Status](https://coveralls.io/repos/mp911de/spinach/badge.svg?branch=master)](https://coveralls.io/r/mp911de/spinach?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/biz.paluch.redis/spinach/badge.svg)](https://maven-badges.herokuapp.com/maven-central/biz.paluch.redis/spinach)

spinach - A scalable Java Disque client
=============

Spinach is a scalable thread-safe Disque client providing both synchronous and
asynchronous APIs. Multiple threads may share one connection if they do not use blocking commands. Spinach is based on
[lettuce](https://github.com/mp911de/lettuce).
Multiple connections are efficiently managed by the excellent netty NIO
framework.

* Works with Java 6, 7 and 8
* [synchronous](https://github.com/mp911de/spinach/wiki/Basic-usage), [asynchronous](https://github.com/mp911de/spinach/wiki/Asynchronous-API) and [reactive](https://github.com/mp911de/spinach/wiki/Reactive-API) APIs
* [SSL](https://github.com/mp911de/spinach/wiki/SSL-Connections) and [Unix Domain Socket](https://github.com/mp911de/spinach/wiki/Unix-Domain-Sockets) connections
* [Codecs](https://github.com/mp911de/lettuce/wiki/Codecs) (for UTF8/bit/JSON etc. representation of your data)

See the [Wiki](https://github.com/mp911de/spinach/wiki) for more docs.

Communication
---------------

* [Github Issues](https://github.com/mp911de/spinach/issues)


Documentation
---------------

* [Wiki](https://github.com/mp911de/spinach/wiki)
* [Javadoc](http://spinach.paluch.biz/apidocs/)

Binaries/Download
----------------

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at http://search.maven.org.

Releases of spinach are available in the maven central repository. Take also a look at the [Download](https://github.com/mp911de/spinach/wiki/Download) page in the [Wiki](https://github.com/mp911de/lettuce/wiki).

Example for Maven:

```xml
<dependency>
  <groupId>biz.paluch.redis</groupId>
  <artifactId>spinach</artifactId>
  <version>x.y.z</version>
</dependency>
```

All versions: [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22biz.paluch.redis%22%20AND%20a%3A%spinach%22)

Snapshots: [Sonatype OSS Repository](https://oss.sonatype.org/#nexus-search;gav~biz.paluch.redis~spinach~~~) 


Basic Usage
-----------

```java
DisqueClient client = DisqueClient.create(DisqueURI.create("host", 7711));
DisqueConnection<String, String> connection = client.connect().sync();
DisqueCommands<String, String> sync = connection.sync();
String jobId = sync.addjob("queue", "body", 1, TimeUnit.MINUTES);
  
Job<String, String> job = sync.getjob("queue");
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

Asynchronous API
------------------------

```java
DisqueConnection<String, String> connection = client.connect();
DisqueAsyncCommands<String, String>  async = connection.async();
RedisFuture<String> jobId1 = async.addjob("queue", "body1", 1, SECONDS);
RedisFuture<String> jobId2 = async.addjob("queue", "body2", 1, SECONDS);

async.awaitAll(jobId1, jobId2) == true

jobId1.get() == "D-..."
jobId2.get() == "D-..."
 ```

Building
-----------

Spinach is built with Apache Maven. The tests require multiple running Disque instances for different test cases which
are configured using a ```Makefile```.

* Initial environment setup (clone and build `disque`, setup SSL keys for SSL tests): ```make prepare ssl-keys```
* Run the build: ```make test```
* Start Disque (manually): ```make start```
* Stop Disque (manually): ```make stop```

License
-------

* [Apache License 2.0] (http://www.apache.org/licenses/LICENSE-2.0)

Contributing
-------

Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in and take a look into [CONTRIBUTING.md](https://github.com/mp911de/spinach/tree/master/CONTRIBUTING.md)
