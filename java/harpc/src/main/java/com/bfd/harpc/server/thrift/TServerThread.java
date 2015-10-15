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
package com.bfd.harpc.server.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;

/**
 * 服务线程
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-13
 */
public class TServerThread extends Thread {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** {@link TServer} */
    private final TServer server;

    /**
     * @param processor
     *            {@link TProcessor}
     * @param serverNode
     *            {@link ServerNode}
     * @param maxWorkerThreads
     *            最大工作线程数
     * @param minWorkerThreads
     *            最小工作线程数
     * @param monitor
     *            {@link RpcMonitor}
     * @throws RpcException
     */
    public TServerThread(TProcessor processor, ServerNode serverNode, int maxWorkerThreads, int minWorkerThreads, RpcMonitor monitor) throws RpcException {
        TServerSocket serverTransport;
        try {
            serverTransport = new TServerSocket(serverNode.getPort());
        } catch (TTransportException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, e);
        }
        Factory portFactory = new TBinaryProtocol.Factory(true, true);
        Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(portFactory);
        args.maxWorkerThreads(maxWorkerThreads);
        args.minWorkerThreads(minWorkerThreads);
        server = new TThreadPoolServer(args);
        server.setServerEventHandler(new ThriftEventHandler(monitor, serverNode));
        setName("Harpc-Thrift-Server");
    }

    /**
     * @param tServer
     *            {@link TServer}
     */
    public TServerThread(TServer tServer) {
        this.server = tServer;
    }

    @Override
    public void run() {
        try {
            System.out.println("Server is start!");
            LOGGER.info("Server is start!");
            server.serve();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 停止服务
     * <p>
     */
    public void stopServer() {
        server.stop();
        System.out.println("Server is stop!");
        LOGGER.info("Server is stop!");
    }

    /**
     * TServer是否启动
     * <p>
     * 
     * @return
     */
    public boolean isStarted() {
        return server.isServing();
    }
}
