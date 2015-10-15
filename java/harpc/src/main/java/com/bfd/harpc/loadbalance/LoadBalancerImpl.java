/**
 * Copyright (C) 2015 Baifendian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bfd.harpc.loadbalance;

import java.util.Collection;
import java.util.Set;

import com.bfd.harpc.loadbalance.common.Closure;
import com.bfd.harpc.loadbalance.common.DynamicSet;
import com.bfd.harpc.loadbalance.common.HostChangeMonitor;
import com.bfd.harpc.loadbalance.common.ResourceExhaustedException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class LoadBalancerImpl<K> implements LoadBalancer<K> {

    private final LoadBalancingStrategy<K> strategy;

    private Set<K> offeredBackends = ImmutableSet.of();

    /**
     * Creates a new load balancer that will use the given strategy.
     * 
     * @param strategy
     *            Strategy to delegate load balancing work to.
     * @param hostSet
     */
    public LoadBalancerImpl(LoadBalancingStrategy<K> strategy, DynamicSet<K> hostSet) {
        this.strategy = Preconditions.checkNotNull(strategy);
        if (hostSet != null) {
            hostSet.monitor(new HostChangeMonitor<K>() {
                @Override
                public void onChange(ImmutableSet<K> hostAndPorts) {
                    offerBackends(hostAndPorts);
                }
            });
        }
    }

    @Override
    public synchronized void offerBackends(Set<K> offeredBackends) {
        this.offeredBackends = ImmutableSet.copyOf(offeredBackends);
        this.strategy.offerBackends(this.offeredBackends, new Closure<Collection<K>>() {
            @Override
            public void execute(Collection<K> item) {

            }
        });
    }

    @Override
    public synchronized K nextBackend() throws ResourceExhaustedException {
        return strategy.nextBackend();
    }

    @Override
    public void requestResult(K key, RequestResult result, long requestTimeNanos) {
        this.strategy.addConnectResult(key, result, requestTimeNanos);
    }

    /**
     * Convenience method to create a new load balancer.
     * 
     * @param strategy
     *            Strategy to use.
     * @param <K>
     *            Backend type.
     * @return A new load balancer.
     */
    public static <K> LoadBalancerImpl<K> create(LoadBalancingStrategy<K> strategy, DynamicSet<K> hostSet) {
        return new LoadBalancerImpl<K>(strategy, hostSet);
    }

}
