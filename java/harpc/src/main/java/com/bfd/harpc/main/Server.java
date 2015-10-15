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
package com.bfd.harpc.main;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.configure.PropertiesConfiguration;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.config.ServerConfig;

/**
 * Server封装类
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-17
 */
public class Server {

    /** {@link RegistryConfig} */
    private final RegistryConfig registryConfig;

    /** {@link ServerConfig} */
    private final ServerConfig serverConfig;

    /**
     * 配置文件路径说明：<br>
     * 1. 使用file，classpath和classpath*做路径开头 <br>
     * 2. classpath寻址项目中的文件 <br>
     * 3. classpath*既寻址项目，也寻址jar包中的文件 <br>
     * 4. file寻址文件系统中的文件:如：D:\\config.properties,/etc/config. properties <br>
     * 5. 默认是classpath <br>
     * 6. 例如：classpath*:log/log4j.xml;file:/home/bfd/abc.sh;classpath:log/log4j.
     * xml
     * <p>
     * 
     * @param configs
     *            配置文件列表，目前只使用第一个配置文件
     * @param impl
     *            接口具体实现类
     * @throws RpcException
     */
    public Server(String[] configs, Object impl) throws RpcException {
        PropertiesConfiguration configuration = PropertiesConfiguration.newInstance(configs[0]);

        // 初始化registry
        registryConfig = new RegistryConfig();
        ConfigHelper.initConfig(registryConfig, "registry.", configuration);

        // 初始化server
        serverConfig = new ServerConfig();
        serverConfig.setRef(impl);
        ConfigHelper.initConfig(serverConfig, "server.", configuration);
    }

    /**
     * 该构造函数重载{@link #Server(String[], Object)}，目的是复用{@link RegistryConfig} <br>
     * 
     * <pre>
     * <b>使用时参考类似如下的代码：</b><code>
     * Server server= new Server(configs,impl);
     * Server server1= new Server(configs1,server.getRegistryConfig());
     * </code>
     * </pre>
     * 
     * @param configs
     *            配置文件列表，目前只使用第一个配置文件
     * @param impl
     *            接口具体实现类
     * @param registryConfig
     *            {@link #getRegistryConfig()}
     * @throws RpcException
     */
    public Server(String[] configs, Object impl, RegistryConfig registryConfig) throws RpcException {
        PropertiesConfiguration configuration = PropertiesConfiguration.newInstance(configs[0]);

        // 初始化registry
        this.registryConfig = registryConfig;

        // 初始化server
        serverConfig = new ServerConfig();
        serverConfig.setRef(impl);
        ConfigHelper.initConfig(serverConfig, "server.", configuration);
    }

    /**
     * 启动服务 <br>
     * <b>注意:</b>本函数属于异步启动，如需要保持服务一直运行，需要主动阻塞主线程。 <br>
     * 
     * <pre>
     * 阻塞方法可参考如下方式：
     * <code>
     *  synchronized (ServiceMain.class) {
     *                 while (running) {
     *                     try {
     *                         ServiceMain.class.wait();
     *                     } catch (Throwable e) {
     *                     }
     *                 }
     *             }
     * </code>
     * </pre>
     * <p>
     * 
     * @throws RpcException
     * @throws ClassNotFoundException
     */
    public void start() throws ClassNotFoundException, RpcException {
        serverConfig.export(registryConfig);
    }

    /**
     * (显式)关闭服务<br>
     * <b>注意:</b>Server启动时，会在addShutdownHook中添加关闭事件，所以使用kill关闭程序时是不需要调用close的。
     * 这里主要提供给需要在程序中显示关闭服务情形，这时可以显式调用close关闭服务。
     * <p>
     */
    public void close() {
        serverConfig.destory();
    }

    /**
     * getter method
     * 
     * @see Server#registryConfig
     * @return the registryConfig
     */
    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

}
