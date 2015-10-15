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
import com.bfd.harpc.config.ClientConfig;
import com.bfd.harpc.config.RegistryConfig;

/**
 * Client封装类
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-17
 */
public class Client<T> {

    /** {@link RegistryConfig} */
    private final RegistryConfig registryConfig;

    /** {@link ClientConfig} */
    private final ClientConfig<T> clientConfig;

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
     * @throws RpcException
     */
    public Client(String[] configs) throws RpcException {
        PropertiesConfiguration configuration = PropertiesConfiguration.newInstance(configs[0]);

        // 初始化registry
        registryConfig = new RegistryConfig();
        ConfigHelper.initConfig(registryConfig, "registry.", configuration);

        // 初始化client
        clientConfig = new ClientConfig<T>();
        ConfigHelper.initConfig(clientConfig, "client.", configuration);
    }

    /**
     * 该构造函数重载{@link #Client(String[])}，目的是复用{@link RegistryConfig} <br>
     * 
     * <pre>
     * <b>使用时参考类似如下的代码：</b><code>
     * Client&ltTObject&gt client= new Client&ltTObject&gt(configs);
     * Client&ltTObject&gt client1= new Client&ltTObject&gt(configs1,client.getRegistryConfig());
     * </code>
     * </pre>
     * 
     * @param configs
     *            配置文件列表，目前只使用第一个配置文件
     * @param registryConfig
     *            {@link #getRegistryConfig()}
     * @throws RpcException
     */
    public Client(String[] configs, RegistryConfig registryConfig) throws RpcException {
        PropertiesConfiguration configuration = PropertiesConfiguration.newInstance(configs[0]);

        // 初始化registry
        this.registryConfig = registryConfig;

        // 初始化client
        clientConfig = new ClientConfig<T>();
        ConfigHelper.initConfig(clientConfig, "client.", configuration);
    }

    /**
     * 创建代理<br>
     * <b>注意:</b>代理内部已经使用连接池，所以这里只需要创建一个实例，多线程共享；特殊情况下，可以允许创建多个实例，
     * 但严禁每次调用前都创建一个实例。
     */
    public T createProxy() throws Exception {
        return clientConfig.createProxy(registryConfig);
    }

    /**
     * (显式)关闭client，释放资源<br>
     * <b>注意:</b>createProxy()时，会在addShutdownHook中添加关闭事件，
     * 所以使用kill关闭程序时是不需要调用close的。 这里主要提供给需要在程序中显示释放资源的情形，这时可以显式调用close释放资源。
     * <p>
     */
    public void close() {
        clientConfig.destory();
    }

    /**
     * getter method
     * 
     * @see Client#registryConfig
     * @return the registryConfig
     */
    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

}
