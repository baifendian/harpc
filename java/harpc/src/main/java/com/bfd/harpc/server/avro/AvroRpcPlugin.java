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

import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.RPCPlugin;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;

/**
 * Avro的rpc服务插件
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-16
 */
public class AvroRpcPlugin extends RPCPlugin {

    /** {@link RpcMonitor} */
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
    public AvroRpcPlugin(RpcMonitor monitor, ServerNode serverNode) {
        // this.monitor = monitor;
        // this.serverNode = serverNode;
    }

    @Override
    public void serverSendResponse(RPCContext context) {
        // if (monitor != null) {
        // StatisticsInfo info = new StatisticsInfo();
        // info.setSuccess(1L);
        // monitor.collect(serverNode, info);
        // }
    }
}
