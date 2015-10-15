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

import org.apache.commons.lang.StringUtils;

import com.bfd.harpc.heartbeat.HeartBeatManager;
import com.bfd.harpc.loadbalance.common.DynamicSet;

/**
 * 负载均衡器简单工厂
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-10
 */
public class LoadBalancerFactory {

    /**
     * private constructor
     */
    private LoadBalancerFactory() {
    }

    /**
     * 创建负载均衡器
     * <p>
     * 
     * @param backends
     * @param strategyName
     * @return {@link LoadBalancer}
     */
    public static <K, T> LoadBalancer<K> createLoadBalancer(DynamicSet<K> backends, String strategyName, HeartBeatManager<T> heartBeatManager) {
        LoadBalancingStrategy<K> loadBalancingStrategy = createLoadBalancingStrategy(strategyName, heartBeatManager);

        return LoadBalancerImpl.create(loadBalancingStrategy, backends);
    }

    /**
     * 创建负载均衡策略
     * <p>
     * 
     * @param strategyName
     * @return {@link LoadBalancingStrategy}
     */
    private static <K, T> LoadBalancingStrategy<K> createLoadBalancingStrategy(String strategyName, HeartBeatManager<T> heartBeatManager) {
        LoadBalancingStrategy<K> strategy = null;
        if (StringUtils.isEmpty(strategyName) || "round".equals(strategyName)) {
            strategy = new RoundRobinStrategy<K>();
        } else {
            strategy = new RandomStrategy<K>();
        }

        return strategy;
    }

}
