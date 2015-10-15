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

abstract class StaticLoadBalancingStrategy<K> implements LoadBalancingStrategy<K> {

    @Override
    public final void offerBackends(Set<K> offeredBackends, Closure<Collection<K>> onBackendsChosen) {
        onBackendsChosen.execute(onBackendsOffered(offeredBackends));
    }

    protected abstract Collection<K> onBackendsOffered(Set<K> offeredBackends);

    @Override
    public void addConnectResult(K backendKey, RequestTracker.RequestResult result, long connectTimeNanos) {
        // No-op.
    }

}
