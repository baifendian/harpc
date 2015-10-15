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

import java.io.IOException;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.config.ServerConfig;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-22
 */
public class ServiceMain {
    public static void main(String[] args) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAuth("admin:admin123");

        // registryConfig.setConnectstr("127.0.0.1:2181");
        registryConfig.setConnectstr("172.18.1.22:2181");

        EchoServiceImpl echoServiceImpl = new EchoServiceImpl();
        // MessageProtocolImpl protocolImpl = new MessageProtocolImpl();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(19091);
        // serverConfig.setProtocol("avro");
        serverConfig.setName("demo");
        serverConfig.setRef(echoServiceImpl);
        // serverConfig.setRef(protocolImpl);
        // serverConfig.setMonitor(true);
        serverConfig.setMaxWorkerThreads(100);
        serverConfig.setInterval(60);
        serverConfig.setMonitor(true);
        serverConfig.setService("com.bfd.harpc.test$EchoService");
        // serverConfig.setService("com.bfd.rpc.avro.test$EchoService");

        try {
            serverConfig.export(registryConfig);
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RpcException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
