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
//package com.bfd.harpc.loadbalance;
//
//import java.util.Collection;
//import java.util.Deque;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.bfd.harpc.loadbalance.common.Closure;
//import com.bfd.harpc.loadbalance.common.ResourceExhaustedException;
//import com.google.common.collect.Queues;
//import com.google.common.collect.Sets;
//
//public class MarkDeadStrategy<K, T> implements LoadBalancingStrategy<K> {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(MarkDeadStrategy.class);
//
//    // private static final int WAIT_TIME = 10 * 1000;
//
//    private final LoadBalancingStrategy<K> wrappedStrategy;
//
//    private final Deque<K> deadBackends = Queues.newArrayDeque();
//
//    private Closure<Collection<K>> onBackendsChosen;
//
//    private volatile Set<K> liveBackends;
//
//    private volatile Set<K> backupLiveBackends;
//
//    // private final Thread recover;
//
//    public MarkDeadStrategy(LoadBalancingStrategy<K> wrappedStrategy) {
//        this.wrappedStrategy = wrappedStrategy;
//        // recover = new Thread(new DeadRecover(), "Harpc-MarkDead-Recover");
//        // recover.setDaemon(true);
//        // recover.start();
//    }
//
//    @Override
//    public void offerBackends(Set<K> offeredBackends, Closure<Collection<K>> onBackendsChosen) {
//        this.liveBackends = Sets.newHashSet(offeredBackends);
//        this.backupLiveBackends = Sets.newHashSet(offeredBackends);
//        this.onBackendsChosen = onBackendsChosen;
//        // reset the backends;
//        this.deadBackends.clear();
//        wrappedStrategy.offerBackends(offeredBackends, onBackendsChosen);
//    }
//
//    @Override
//    public K nextBackend() throws ResourceExhaustedException {
//        return wrappedStrategy.nextBackend();
//    }
//
//    @Override
//    public void addConnectResult(K key, RequestTracker.RequestResult result, long connectTimeNanos) {
//        switch (result) {
//            case DEAD:
//            case TIMEOUT:
//                addDeadBackends(key);
//                break;
//            case SUCCESS:
//                break;
//            default:
//        }
//    }
//
//    private void addDeadBackends(final K dead) {
//        if (dead != null) {
//            // log.warn("want add [{}] to dead list", dead);
//            if (!deadBackends.contains(dead)) {
//                deadBackends.push(dead);
//                LOGGER.warn("add [{}] to dead list", dead);
//            }
//            if (liveBackends != null) {
//                if (liveBackends.size() > 1) {
//                    liveBackends.remove(dead);
//                    LOGGER.warn("remove [{}] from live list", dead);
//                    if (liveBackends.isEmpty()) {
//                        adjustBackends();
//                    }
//                    wrappedStrategy.offerBackends(liveBackends, onBackendsChosen);
//                }
//            }
//        }
//    }
//
//    private void adjustBackends() {
//        liveBackends = Sets.newHashSet(backupLiveBackends);
//        LOGGER.warn("add backupLiveBackends [{}] to live list", backupLiveBackends);
//    }
//
//    /**
//     * recover线程，暂不使用
//     * 
//     * <pre>
//     * private class DeadRecover implements Runnable {
//     * 
//     *     &#064;Override
//     *     public void run() {
//     *         try {
//     *             while (true) {
//     *                 if (deadBackends.size() &gt; 0) {
//     *                     while (!deadBackends.isEmpty()) {
//     *                         K dead = deadBackends.poll();
//     *                         // XXX:heartbeat中不可用则直接不可用，不必恢复
//     *                         if (dead != null &amp;&amp; !heartBeatManager.isDeadByHeartbeat(dead)) {
//     *                             liveBackends.add(dead);
//     *                             LOGGER.warn(&quot;try to add dead[{}] to live list&quot;, dead);
//     *                         }
//     *                     }
//     * 
//     *                     // heartbeat中的不可用集合加入到不可用队列
//     *                     Iterator&lt;K&gt; iterator = heartBeatManager.&lt;K&gt; getDeads().iterator();
//     *                     while (iterator.hasNext()) {
//     *                         K k = iterator.next();
//     *                         deadBackends.push(k);
//     *                     }
//     * 
//     *                     wrappedStrategy.offerBackends(liveBackends, onBackendsChosen);
//     * 
//     *                 }
//     *                 Thread.sleep(WAIT_TIME);
//     *             }
//     *         } catch (Exception e) {
//     *             LOGGER.warn(&quot;fail to recover dead endpoint&quot;, e);
//     *         }
//     *     }
//     * }
//     * </pre>
//     */
//
// }