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

import com.bfd.harpc.config.ClientConfig;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.test.gen.EchoService.Iface;

/**
 * The demo of client by api.
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-9-1
 */
public class ClientDemo {
    /**
     * @param args
     */
    public static void main(String[] args) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setConnectstr("172.18.1.22:2181");
        registryConfig.setAuth("admin:admin123");

        String iface = Iface.class.getName();
        ClientConfig<Iface> clientConfig = new ClientConfig<Iface>();
        clientConfig.setService("com.bfd.harpc.demo$EchoService");
        clientConfig.setIface(iface);
        // clientConfig.setAddress("172.18.1.23:19090;172.18.1.24:19090");

        try {
            // 注意:代理内部已经使用连接池，所以这里只需要创建一个实例，多线程共享；特殊情况下，可以允许创建多个实例，
            // 但严禁每次调用前都创建一个实例。
            Iface echoService = clientConfig.createProxy(registryConfig);

            for (int i = 0; i < 10; i++) {
                System.out.println(echoService.echo("world!"));
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
