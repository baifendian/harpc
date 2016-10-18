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

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.config.ServerConfig;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.registry.IRegistry;
import com.bfd.harpc.registry.ZkServerRegistry;
import com.bfd.harpc.server.IServer;

/**
 * 服务提供者javabean
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-18
 */
public class ServerBean extends ServerConfig implements ApplicationContextAware, InitializingBean {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** javabean的ID */
    private String id;

    /** {@link ApplicationContext} */
    private ApplicationContext applicationContext;

    /**
     * getter method
     * 
     * @see ServerConfig#id
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * setter method
     * 
     * @see ServerConfig#id
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

    @Override
    public void afterPropertiesSet() throws Exception {
        export();

    }

    /**
     * 暴露服务
     * <p>
     * 
     * @throws ClassNotFoundException
     *             ,RpcException
     */
    @SuppressWarnings({ "unchecked" })
    public void export() throws ClassNotFoundException, RpcException {
        // 参数检查
        check();

        // 获取zkClient
        CuratorFramework zkClient = null;
        String auth = null;
        if (applicationContext != null) {
            Map<String, RegistryConfig> regMap = applicationContext.getBeansOfType(RegistryConfig.class);
            if (regMap != null && regMap.size() > 0) {
                for(RegistryConfig config : regMap.values()){
                    if ( config != null ){
                        try {
                            zkClient = config.obtainZkClient();
                            auth = config.getAuth();
                            if (StringUtils.isEmpty(auth)) {
                                throw new RpcException(RpcException.CONFIG_EXCEPTION, "The params 'auth' cannot empty!");
                            }
                        } catch (Exception e) {
                            throw new RpcException("Registry error!", e);
                        }
                        break;
                    }
                }
                /* 一些小的性能改进
                for (String key : regMap.keySet()) {
                    if (regMap.get(key) != null) {
                        try {
                            zkClient = regMap.get(key).obtainZkClient();
                            auth = regMap.get(key).getAuth();
                            if (StringUtils.isEmpty(auth)) {
                                throw new RpcException(RpcException.CONFIG_EXCEPTION, "The params 'auth' cannot empty!");
                            }
                        } catch (Exception e) {
                            throw new RpcException("Registry error!", e);
                        }
                        break;
                    }
                }
                */
            }
        }

        // 服务注册
        IRegistry registry = null;
        ServerNode serverNode = genServerNode();
        if (zkClient != null) {
            registry = new ZkServerRegistry(zkClient, getService(), serverNode.genAddress(), auth);
        }

        // 创建监控
        RpcMonitor rpcMonitor = null;
        if (isMonitor()) {
            rpcMonitor = new RpcMonitor(getInterval(), zkClient, getService(), false);
        }

        // 创建服务
        IServer server = createServer(serverNode, rpcMonitor);
        server.start();

        if (server.isStarted()) {
            try {
                // 服务注册
                registry.register(genConfigJson());
                // 添加关闭钩子
                addShutdownHook(registry, server);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                server.stop();
            }
        } else {
            server.stop();
        }
    }

}
