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
package biz.paluch.spinach.support;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.TestSettings;

import com.lambdaworks.redis.resource.ClientResources;

/**
 * @author Mark Paluch
 */
public class DefaultDisqueClient {

    public final static DefaultDisqueClient instance = new DefaultDisqueClient();

    private DisqueClient disqueClient;
    private ClientResources clientResources;

    public DefaultDisqueClient() {
        clientResources = TestClientResources.create();
        disqueClient = DisqueClient.create(clientResources, DisqueURI.Builder.disque(TestSettings.host(), TestSettings.port())
                .build());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FastShutdown.shutdown(disqueClient);
            }
        });
    }

    /**
     * Do not close the client.
     * 
     * @return the default disque client for the tests.
     */
    public static DisqueClient get() {
        instance.disqueClient.setDefaultTimeout(60, TimeUnit.SECONDS);
        return instance.disqueClient;
    }

    /**
     * Do not close the client resources.
     * @return the default client resources for the tests.
     */
    public static ClientResources getClientResources() {
        return instance.clientResources;
    }
}
