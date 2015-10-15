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
package com.bfd.harpc.server.avro;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Responder;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.server.IServer;

/**
 * Avro服务
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-16
 */
public class AvroServer implements IServer {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** 服务线程 */
    private final NettyServer nettyServer;

    /** 是否已经启动 */
    private boolean isStarted = false;

    /**
     * @param responder
     *            {@link Responder}
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
    public AvroServer(Responder responder, ServerNode serverNode, int maxWorkerThreads, int minWorkerThreads, RpcMonitor monitor) throws RpcException {
        ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        responder.addRPCPlugin(new AvroRpcPlugin(monitor, serverNode));
        this.nettyServer = new NettyServer(responder, new InetSocketAddress(serverNode.getPort()), channelFactory);
    }

    @Override
    public void start() {
        if (!isStarted) {
            nettyServer.start();// 非阻塞
            isStarted = true;
            System.out.println("Server is start!");
            LOGGER.info("Server is start!");
        }
    }

    @Override
    public void stop() {
        if (isStarted) {
            nettyServer.close();
            isStarted = false;
            System.out.println("Server is closed!");
            LOGGER.info("Server is closed!");
        }
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

}
