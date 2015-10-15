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
package com.bfd.harpc.test.demo.api;

import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.config.ServerConfig;
import com.bfd.harpc.test.demo.DemoServiceImpl;

/**
 * The demo of server by api.
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-9-1
 */
public class ServerDemo {
    /**
     * @param args
     */
    public static void main(String[] args) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setConnectstr("172.18.1.22:2181");
        registryConfig.setAuth("admin:admin123");
        registryConfig.setTimeout(5000);

        DemoServiceImpl serviceImpl = new DemoServiceImpl();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(19090);
        serverConfig.setRef(serviceImpl);
        serverConfig.setService("com.bfd.harpc.demo$EchoService");

        try {
            serverConfig.export(registryConfig); // 暴露服务
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
