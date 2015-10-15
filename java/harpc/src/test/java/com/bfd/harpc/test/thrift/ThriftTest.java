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
package com.bfd.harpc.test.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.server.thrift.TServerThread;
import com.bfd.harpc.test.EchoServiceImpl;
import com.bfd.harpc.test.gen.EchoService;
import com.bfd.harpc.test.gen.EchoService.Iface;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-8
 */
public class ThriftTest {
    public static void main(String[] args) throws InterruptedException {
        TServerSocket serverTransport;
        try {
            serverTransport = new TServerSocket(19091);
        } catch (TTransportException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, e);
        }
        Factory portFactory = new TBinaryProtocol.Factory(true, true);
        Args arg = new Args(serverTransport);
        Iface echoService = new EchoServiceImpl();
        TProcessor processor = new EchoService.Processor<Iface>(echoService);
        arg.processor(processor);
        arg.protocolFactory(portFactory);
        arg.maxWorkerThreads(100); // 实际中需要配置
        arg.minWorkerThreads(10);
        TServer server = new TThreadPoolServer(arg);
        new TServerThread(server).start();
        while (true) {
            System.out.println(server.isServing());
            Thread.sleep(1000);

        }
    }
}
