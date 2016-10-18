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
package com.bfd.harpc.monitor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.bfd.harpc.common.Constants;
import com.bfd.harpc.common.ServerNode;

/**
 * rpc监控
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-2
 */
public class RpcMonitor {
    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcMonitor.class);

    /** 节点数目的限制 */
    private static final int NODE_COUNT_LIMIT = 600;

    /** 定时任务执行器 */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3, new NamedThreadFactory("Harpc-SendStatisticsTimer", true));

    /** 统计信息发送定时器 */
    private final ScheduledFuture<?> sendFuture;

    /** 监控的时间间隔(ms),默认5Min */
    private final long monitorInterval;

    /** {@link CuratorFramework} */
    private final CuratorFramework zkClient;

    /** 服务名称 */
    private final String serverName;

    /** 是否client端，否则为server端 */
    private final boolean isClient;

    /** 统计信息 */
    private final ConcurrentMap<ServerNode, AtomicReference<StatisticsInfo>> statisticsMap = new ConcurrentHashMap<ServerNode, AtomicReference<StatisticsInfo>>();

    /** 统计信息（总计） */
    private final ConcurrentMap<ServerNode, AtomicReference<StatisticsInfo>> totalStatisticsMap = new ConcurrentHashMap<ServerNode, AtomicReference<StatisticsInfo>>();

    /** 起始统计时间信息 */
    private final ConcurrentMap<ServerNode, Long> startTimeMap = new ConcurrentHashMap<ServerNode, Long>();

    /** 统计信息详情 */
    private final ConcurrentMap<ServerNode, StatisticsTotal> statMap = new ConcurrentHashMap<ServerNode, StatisticsTotal>();

    /** 系统起始时间 */
    private final long beginTime;

    /** 初始时间 */
    private long startTime;

    /**
     * @param interval
     * @param zkClient
     * @param serverName
     * @param isClient
     */
    public RpcMonitor(long interval, CuratorFramework zkClient, String serverName, boolean isClient) {
        this.monitorInterval = interval;
        this.zkClient = zkClient;
        this.serverName = serverName;
        this.isClient = isClient;

        // 启动统计信息收集定时器
        sendFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    // 发送统计信息
                    send();
                } catch (Throwable t) {
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }, monitorInterval, monitorInterval, TimeUnit.SECONDS);
        beginTime = startTime = System.currentTimeMillis();
    }

    /**
     * 发送统计信息
     * <p>
     */
    protected void send() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start send statistics to zookeeper!");
        }

        // 时间段汇总
        for (Map.Entry<ServerNode, AtomicReference<StatisticsInfo>> entry : statisticsMap.entrySet()) {
            // 获取已统计数据
            ServerNode serverNode = entry.getKey();
            AtomicReference<StatisticsInfo> reference = entry.getValue();
            StatisticsInfo numbers;
            do {
                numbers = reference.get();
            } while (!reference.compareAndSet(numbers, null)); // 重新记录

            StatisticsInfo info = new StatisticsInfo();
            if (numbers != null) {
                info.setSuccess(numbers.getSuccess());
                info.setFailure(numbers.getFailure());
                info.setMaxtime(numbers.getMaxtime());
                info.setMintime(numbers.getMintime());
                info.setAvgtime(numbers.getAvgtime());

                startTimeMap.putIfAbsent(serverNode, startTime);
                long useTime = System.currentTimeMillis() - startTimeMap.get(serverNode);
                startTimeMap.put(serverNode, System.currentTimeMillis()); // 重置起始时间

                info.setQps(1000 / (useTime / (float) (numbers.getSuccess() + numbers.getFailure())));
            }

            // 发送时间段汇总信息
            sendToZookeeper(serverNode, info, false);
        }
        // 重置起始时间（注意：考虑到client端servernode可能会发生变化，这里是必要的）
        startTime = System.currentTimeMillis();

        // 总计
        for (Map.Entry<ServerNode, AtomicReference<StatisticsInfo>> entry : totalStatisticsMap.entrySet()) {
            // 获取已统计数据
            ServerNode serverNode = entry.getKey();
            AtomicReference<StatisticsInfo> reference = entry.getValue();
            StatisticsInfo numbers = reference.get();

            StatisticsInfo info = new StatisticsInfo();
            if (numbers != null) {
                info.setSuccess(numbers.getSuccess());
                info.setFailure(numbers.getFailure());
                info.setMaxtime(numbers.getMaxtime());
                info.setMintime(numbers.getMintime());
                info.setAvgtime(numbers.getAvgtime());
                long useTime = System.currentTimeMillis() - beginTime;
                info.setQps(1000 / (useTime / (float) (numbers.getSuccess() + numbers.getFailure())));
            }

            // 发送汇总信息
            sendToZookeeper(serverNode, info, true);
        }
    }

    /**
     * 发送统计信息到zookeeper中
     * <p>
     * 
     * @param serverNode
     * @param info
     * @param isTotal
     */
    private void sendToZookeeper(ServerNode serverNode, StatisticsInfo info, boolean isTotal) {
        StringBuilder parentPath = new StringBuilder();
        parentPath.append(serverName).append(Constants.ZK_SEPARATOR_DEFAULT).append(Constants.ZK_NAMESPACE_STATISTICS);
        if (isClient) {
            parentPath.append(Constants.ZK_SEPARATOR_DEFAULT).append(Constants.ZK_NAMESPACE_CLIENTS);
        } else {
            parentPath.append(Constants.ZK_SEPARATOR_DEFAULT).append(Constants.ZK_NAMESPACE_SERVERS);
        }
        parentPath.append(Constants.ZK_SEPARATOR_DEFAULT).append(serverNode.genAddress());

        String timeStamp = FastDateFormat.getInstance(Constants.ZK_TIME_NODE_FORMAT).format(Calendar.getInstance());
        info.setTime(timeStamp);
        if (isTotal) {
            statMap.putIfAbsent(serverNode, new StatisticsTotal());
            statMap.get(serverNode).setTotal(info);
        } else {
            statMap.putIfAbsent(serverNode, new StatisticsTotal());

            statMap.get(serverNode).getDetail().add(info);
            // 调整节点数目
            StatisticsHelper.adjustNodesByLimit(statMap.get(serverNode).getDetail(), NODE_COUNT_LIMIT);
        }
        String jsonString = JSON.toJSONString(statMap.get(serverNode));
        String path = parentPath.toString();

        try {
            // 创建节点并添加信息
            if (zkClient.checkExists().forPath(path) != null) {
                zkClient.setData().forPath(path, jsonString.getBytes("utf-8"));
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, jsonString.getBytes("utf-8"));
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 收集统计信息
     * <p>
     * 
     * @param serverNode
     *            {@link ServerNode}
     * @param info
     *            {@link StatisticsInfo}
     */
    public void collect(ServerNode serverNode, StatisticsInfo info) {
        // 统计时间段信息
        // 初始化原子引用
        AtomicReference<StatisticsInfo> reference = statisticsMap.get(serverNode);
        if (reference == null) {
            statisticsMap.putIfAbsent(serverNode, new AtomicReference<StatisticsInfo>());
            reference = statisticsMap.get(serverNode);
        }
        // CompareAndSet并发加入统计数据
        updateStatistics(info, reference);

        // 统计总体信息
        // 初始化原子引用
        AtomicReference<StatisticsInfo> totalReference = totalStatisticsMap.get(serverNode);
        if (totalReference == null) {
            totalStatisticsMap.putIfAbsent(serverNode, new AtomicReference<StatisticsInfo>());
            totalReference = totalStatisticsMap.get(serverNode);
        }
        // CompareAndSet并发加入统计数据
        updateStatistics(info, totalReference);
    }

    /**
     * 更新统计信息
     * <p>
     * 
     * @param info
     * @param reference
     */
    private void updateStatistics(StatisticsInfo info, AtomicReference<StatisticsInfo> reference) {
        StatisticsInfo current;
        StatisticsInfo update = new StatisticsInfo();
        do {
            current = reference.get();
            if (current == null) {
                update.setSuccess(info.getSuccess());
                update.setFailure(info.getFailure());
                update.setMaxtime(info.getMaxtime());
                update.setMintime(info.getMintime());
                update.setAvgtime(info.getAvgtime());
            } else {
                update.setSuccess(current.getSuccess() + info.getSuccess());
                update.setFailure(current.getFailure() + info.getFailure());
                update.setMaxtime(current.getMaxtime() > info.getMaxtime() ? current.getMaxtime() : info.getMaxtime());
                update.setMintime(current.getMintime() < info.getMintime() ? current.getMintime() : info.getMintime());
                update.setAvgtime((current.getAvgtime() * (update.getSuccess() + update.getFailure()) + info.getAvgtime()) / (update.getSuccess() + update.getFailure() + 1));
            }
        } while (!reference.compareAndSet(current, update));
    }

    /**
     * 任务销毁
     * <p>
     */
    public void destroy() {
        try {
            sendFuture.cancel(true);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

}
