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
package com.bfd.harpc.heartbeat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;
import com.bfd.harpc.monitor.NamedThreadFactory;

/**
 * heartbeat管理器
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-17
 */
public class HeartBeatManager<T> {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** {@link ScheduledExecutorService} */
    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Harpc-Heartbeat", true));

    /** {@link DynamicHostSet} */
    private final DynamicHostSet dynamicHostSet;

    /** 心跳定时器 */
    private ScheduledFuture<?> heatbeatTimer;

    /** 心跳频率，毫秒。默认0，不会执行心跳。 */
    private final int heartbeat;

    /** 心跳执行的超时时间 */
    private final int heartbeatTimeout;

    /** 重试次数 */
    private final int times;

    /** 重试间隔 */
    private final int interval;

    /** 连接池 */
    private final GenericKeyedObjectPool<ServerNode, T> pool;

    /**
     * @param dynamicHostSet
     * @param heartbeat
     * @param heartbeatTimeout
     * @param times
     * @param interval
     * @param pool
     */
    public HeartBeatManager(DynamicHostSet dynamicHostSet, int heartbeat, int heartbeatTimeout, int times, int interval, GenericKeyedObjectPool<ServerNode, T> pool) {
        this.dynamicHostSet = dynamicHostSet;
        this.heartbeat = heartbeat;
        this.heartbeatTimeout = heartbeatTimeout;
        this.times = times;
        this.interval = interval;
        this.pool = pool;
    }

    /**
     * 启动heartbeat
     * <p>
     */
    public void startHeatbeatTimer() {
        stopHeartbeatTimer();
        if (heartbeat > 0) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(new HeartBeatTask<T>(dynamicHostSet, times, interval, heartbeatTimeout, pool), heartbeat, heartbeat,
                                                             TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 停止heartbeat
     * <p>
     */
    public void stopHeartbeatTimer() {
        try {
            ScheduledFuture<?> timer = heatbeatTimer;
            if (timer != null && !timer.isCancelled()) {
                timer.cancel(true);
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        } finally {
            heatbeatTimer = null;
        }
    }
}
