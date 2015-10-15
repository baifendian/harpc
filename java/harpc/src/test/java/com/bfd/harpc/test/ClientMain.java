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
package com.bfd.harpc.test;

import com.bfd.harpc.config.ClientConfig;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.test.gen.EchoService;
import com.bfd.harpc.test.gen.MessageProtocol;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-21
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        RegistryConfig registryConfig = new RegistryConfig();
        // registryConfig.setConnectstr("127.0.0.1:2181");
        registryConfig.setConnectstr("172.18.1.22:2181");

        String iface = EchoService.Iface.class.getName();
        // String iface = MessageProtocol.class.getName();
        ClientConfig<MessageProtocol> clientConfig = new ClientConfig<MessageProtocol>();
        clientConfig.setService("com.bfd.harpc.test$EchoService");
        clientConfig.setIface(iface);
        clientConfig.setProtocol("thrift");
        // clientConfig.setProtocol("avro");
        clientConfig.setHeartbeat(2000);
        clientConfig.setMonitor(true);
        clientConfig.setInterval(60);
        clientConfig.setRetry(0);

        final EchoService.Iface echo = (EchoService.Iface) clientConfig.createProxy(registryConfig);
        // final MessageProtocol protocol =
        // clientConfig.createProxy(registryConfig);
        for (int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 1; i <= 300; i++) {
                        try {
                            System.out.println(echo.echo("world!"));

                            // System.out.println(protocol.sendMessage("world!"));

                            Thread.sleep(1000);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

    }
}
