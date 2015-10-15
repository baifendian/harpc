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

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.transport.TTransport;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;

/**
 * Thrift事件处理器
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-8
 */
public class ThriftEventHandler implements TServerEventHandler {
    //
    // /** {@link RpcMonitor} */
    // private final RpcMonitor monitor;
    //
    // /** {@link ServerNode} */
    // private final ServerNode serverNode;

    /**
     * @param monitor
     *            {@link RpcMonitor}
     * @param serverNode
     *            {@link ServerNode}
     */
    public ThriftEventHandler(RpcMonitor monitor, ServerNode serverNode) {
        // this.monitor = monitor;
        // this.serverNode = serverNode;
    }

    @Override
    public ServerContext createContext(TProtocol arg0, TProtocol arg1) {
        return null;
    }

    @Override
    public void deleteContext(ServerContext arg0, TProtocol arg1, TProtocol arg2) {

    }

    @Override
    public void preServe() {

    }

    @Override
    public void processContext(ServerContext arg0, TTransport arg1, TTransport arg2) {
        // TODO:进行ip白名单相关的校验
    }

}
