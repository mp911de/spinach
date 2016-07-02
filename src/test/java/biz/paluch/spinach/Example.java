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
package biz.paluch.spinach;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.sync.DisqueCommands;

/**
 * @author Mark Paluch
 */
public class Example {

    public static void main(String[] args) {
        String nodes = System.getenv("TYND_DISQUE_NODES");
        String auth = System.getenv("TYND_DISQUE_AUTH");
        DisqueClient disqueClient = new DisqueClient("disque://" + auth + "@" + nodes);

        DisqueCommands<String, String> connection = disqueClient.connect().sync();
        System.out.println(connection.ping());
        connection.close();
        disqueClient.shutdown();

    }
}
