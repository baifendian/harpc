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
package com.bfd.harpc.config.spring;

import java.util.Map;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.curator.framework.CuratorFramework;
import org.apache.thrift.TServiceClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.fastjson.annotation.JSONField;
import com.bfd.harpc.RpcException;
import com.bfd.harpc.client.DefaultInvoker;
import com.bfd.harpc.client.Invoker;
import com.bfd.harpc.common.NetUtils;
import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.config.ClientConfig;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.heartbeat.HeartBeatManager;
import com.bfd.harpc.loadbalance.LoadBalancer;
import com.bfd.harpc.loadbalance.LoadBalancerFactory;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.proxy.DynamicClientHandler;
import com.bfd.harpc.registry.DefaultRegistry;
import com.bfd.harpc.registry.IRegistry;
import com.bfd.harpc.registry.ZkClientRegistry;

/**
 * 服务消费者javabean
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-19
 */
@SuppressWarnings("rawtypes")
public class ClientBean extends ClientConfig implements FactoryBean, ApplicationContextAware {

    /** javabean的id */
    private String id;

    /** {@link ApplicationContext} */
    private ApplicationContext applicationContext;

    /**
     * getter method
     * 
     * @see ClientBean#id
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * setter method
     * 
     * @see ClientBean#id
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建代理
     * <p>
     * 
     * @return {@link Object}
     */
    @SuppressWarnings("unchecked")
    public Object createProxy() throws Exception {
        check();// 参数检查

        CuratorFramework zkClient = null;
        IRegistry registry = null;
        ServerNode clientNode = new ServerNode(NetUtils.getLocalHost(), 0);
        if (getAddress() != null) {
            registry = new DefaultRegistry(getAddress());
        } else {
            // 获取zkClient
            if (applicationContext != null) {
                Map<String, RegistryConfig> regMap = applicationContext.getBeansOfType(RegistryConfig.class);
                if (regMap != null && regMap.size() > 0) {

                    for(RegistryConfig config:regMap.values()){
                        if ( config != null ){
                            try {
                                zkClient = config.obtainZkClient();
                                registry = new ZkClientRegistry(getService(), zkClient, clientNode);
                            } catch (Exception e) {
                                throw new RpcException("Registry error!", e);
                            }
                            break;
                        }
                    }
                    /* 改进一些微小的性能
                    for (String key : regMap.keySet()) {
                        if (regMap.get(key) != null) {
                            try {
                                zkClient = regMap.get(key).obtainZkClient();
                                registry = new ZkClientRegistry(getService(), zkClient, clientNode);
                            } catch (Exception e) {
                                throw new RpcException("Registry error!", e);
                            }
                            break;
                        }
                    } */
                }
            }
        }
        if (registry == null) {
            throw new RpcException("The param addess and registry config cannot all no exist!");
        }

        registry.register(genConfigJson());

        // 创建监控

        RpcMonitor rpcMonitor = null;
        if (isMonitor()) {
            rpcMonitor = new RpcMonitor(getInterval(), zkClient, getService(), true);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 加载Iface接口
        Class<?> objectClass = classLoader.loadClass(getIface());

        GenericKeyedObjectPool<ServerNode, TServiceClient> pool = bulidClientPool(classLoader, objectClass);

        HeartBeatManager heartBeatManager = new HeartBeatManager(registry.findAllService(), getHeartbeat(), getHeartbeatTimeout(), getHeartbeatTimes(), getHeartbeatInterval(),
                                                                 pool);
        heartBeatManager.startHeatbeatTimer();

        DynamicHostSet hostSet = registry.findAllService();
        LoadBalancer<ServerNode> loadBalancer = LoadBalancerFactory.createLoadBalancer(hostSet, getLoadbalance(), heartBeatManager);

        // 添加ShutdownHook
        addShutdownHook(registry, rpcMonitor, heartBeatManager);

        Invoker invoker = new DefaultInvoker(clientNode, pool, loadBalancer, getRetry(), rpcMonitor, hostSet);
        DynamicClientHandler dynamicClientHandler = new DynamicClientHandler(invoker);
        return dynamicClientHandler.bind(classLoader, objectClass);
    }

    @Override
    @JSONField(serialize = false)
    public Object getObject() throws Exception {
        return createProxy();
    }

    @Override
    @JSONField(serialize = false)
    public Class getObjectType() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 加载Iface接口
        Class<?> objectClass = null;
        try {
            objectClass = classLoader.loadClass(getIface());
        } catch (ClassNotFoundException e) {
        }
        return objectClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
