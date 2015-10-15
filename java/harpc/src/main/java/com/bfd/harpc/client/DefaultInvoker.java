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
package com.bfd.harpc.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;

import org.apache.avro.AvroRemoteException;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.loadbalance.LoadBalancer;
import com.bfd.harpc.loadbalance.RequestTracker;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.monitor.StatisticsInfo;

/**
 * 默认invoker实现
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-3
 */
public class DefaultInvoker<T> implements Invoker {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** {@link GenericKeyedObjectPool}<{@link ServerNode},{@link T}> */
    private final GenericKeyedObjectPool<ServerNode, T> pool;

    /** {@link LoadBalancer}<{@link ServerNode}> */
    private final LoadBalancer<ServerNode> loadBalancer;

    /** 重试次数 */
    private final int retry;

    /** {@link RpcMonitor} */
    private final RpcMonitor monitor;

    /** client端的{@link ServerNode} */
    private final ServerNode clientNode;

    /** {@link DynamicHostSet} */
    private final DynamicHostSet hostSet;

    /**
     * @param clientNode
     * @param pool
     * @param loadBalancer
     * @param retry
     * @param monitor
     * @param hostSet
     */
    public DefaultInvoker(ServerNode clientNode, GenericKeyedObjectPool<ServerNode, T> pool, LoadBalancer<ServerNode> loadBalancer, int retry, RpcMonitor monitor,
                          DynamicHostSet hostSet) {
        this.clientNode = clientNode;
        this.pool = pool;
        this.loadBalancer = loadBalancer;
        this.retry = retry;
        this.monitor = monitor;
        this.hostSet = hostSet;
    }

    @Override
    public Object invoke(Method method, Object[] args) throws RpcException {
        T client = null;
        ServerNode serverNode = null;
        Throwable exception = null;

        long startTime;
        for (int i = 0; i == 0 || i < retry + 1; i++) {
            startTime = System.currentTimeMillis();
            StatisticsInfo info = new StatisticsInfo();
            try {
                serverNode = loadBalancer.nextBackend();
                if (serverNode == null) {
                    continue;
                }

                // System.out.println(serverNode);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Invoke to {}.", serverNode);
                }
                client = pool.borrowObject(serverNode);
                Object result = method.invoke(client, args);

                // 设置load balance和statistics
                loadBalancer.requestResult(serverNode, RequestTracker.RequestResult.SUCCESS, System.currentTimeMillis() - startTime);
                info.setSuccess(1);

                return result;
            } catch (InvocationTargetException ite) {// XXX:InvocationTargetException异常发生在method.invoke()中
                Throwable cause = ite.getCause();
                if (cause != null) {
                    if (cause instanceof TTransportException || cause instanceof AvroRemoteException) {
                        // 设置load balance和statistics
                        loadBalancer.requestResult(serverNode, RequestTracker.RequestResult.TIMEOUT, System.currentTimeMillis() - startTime);
                        hostSet.addDeadInstance(serverNode); // 加入dead集合中
                        info.setFailure(1);
                        exception = cause;
                        try {
                            // XXX:这里直接清空pool,否则会出现连接慢恢复的现象
                            // 发送socket异常时，证明socket已经失效，需要重新创建
                            if (cause.getCause() != null && cause.getCause() instanceof SocketException) {
                                pool.clear(serverNode);
                            } else {
                                // XXX:其他异常的情况，需要将当前链接置为无效
                                pool.invalidateObject(serverNode, client);
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        client = null; // 这里是为了防止后续return回pool中
                    } else {
                        loadBalancer.requestResult(serverNode, RequestTracker.RequestResult.FAILED, System.currentTimeMillis() - startTime);
                        info.setFailure(1);
                        exception = cause;
                    }
                } else {
                    loadBalancer.requestResult(serverNode, RequestTracker.RequestResult.FAILED, System.currentTimeMillis() - startTime);
                    info.setFailure(1);
                    exception = ite;
                }
            } catch (Throwable e) {
                // 设置load balance和statistics
                loadBalancer.requestResult(serverNode, RequestTracker.RequestResult.FAILED, System.currentTimeMillis() - startTime);
                info.setFailure(1);
                exception = e;
            } finally {
                if (client != null) {
                    try {
                        pool.returnObject(serverNode, client);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                // 收集统计信息
                if (monitor != null) {
                    long usetime = System.currentTimeMillis() - startTime;
                    info.setAvgtime(usetime);
                    info.setMaxtime(usetime);
                    info.setMintime(usetime);
                    monitor.collect(clientNode, info);
                }
            }
        }
        throw new RpcException("Invoke error!", exception);
    }
}
