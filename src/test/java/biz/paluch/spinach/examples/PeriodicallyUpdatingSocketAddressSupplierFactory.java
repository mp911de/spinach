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

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.impl.HelloClusterSocketAddressSupplier;
import biz.paluch.spinach.impl.RoundRobinSocketAddressSupplier;
import biz.paluch.spinach.impl.SocketAddressSupplier;
import biz.paluch.spinach.impl.SocketAddressSupplierFactory;
import io.netty.util.internal.ConcurrentSet;

/**
 * @author Mark Paluch
 */
public class PeriodicallyUpdatingSocketAddressSupplierFactory implements SocketAddressSupplierFactory {

    private final ScheduledExecutorService scheduledExecutorService;
    private final Set<ScheduledFuture<?>> futures = new ConcurrentSet<>();

    public PeriodicallyUpdatingSocketAddressSupplierFactory(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {

        RoundRobinSocketAddressSupplier bootstrap = new RoundRobinSocketAddressSupplier(disqueURI.getConnectionPoints());

        HelloClusterSocketAddressSupplier helloCluster = new HelloClusterSocketAddressSupplier(bootstrap) {

            /**
             * This method is called only once when the connection is established.
             */
            @Override
            public <K, V> void setConnection(DisqueConnection<K, V> disqueConnection) {

                Runnable command = new Runnable() {
                    @Override
                    public void run() {
                        reloadNodes();
                    }
                };

                ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(command, 1, 1,
                        TimeUnit.HOURS);

                futures.add(scheduledFuture);
                super.setConnection(disqueConnection);
            }

        };

        return helloCluster;
    }

    /**
     * Shutdown the scheduling.
     */
    public void shutdown() {
        for (ScheduledFuture<?> future : futures) {
            future.cancel(false);
        }
    }
}
