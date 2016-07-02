/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.spinach.examples;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.sync.DisqueCommands;

/**
 * @author Mark Paluch
 */
public class Standalone {

    public static void main(String[] args) {
        DisqueClient disqueClient = DisqueClient.create(DisqueURI.create("disque://password@localhost:7711"));
        DisqueCommands<String, String> sync = disqueClient.connect().sync();

        sync.addjob("queue", "body", 1, TimeUnit.MINUTES);

        Job<String, String> job = sync.getjob("queue");

        sync.ackjob(job.getId());

        sync.close();
        disqueClient.shutdown();
    }
}
