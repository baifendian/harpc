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

import org.apache.avro.Protocol.Message;
import org.apache.avro.ipc.specific.SpecificResponder;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.monitor.StatisticsInfo;

/**
 * 增加监控功能的{@link SpecificResponder} (暂时废弃)
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-7-3
 */
@Deprecated
public class MonitorSpecificResponder extends SpecificResponder {

    /** {@link RpcMonitor} */
    private final RpcMonitor monitor;

    /** {@link ServerNode} */
    private final ServerNode serverNode;

    /**
     * @param iface
     * @param impl
     * @param monitor
     * @param serverNode
     */
    @SuppressWarnings("rawtypes")
    public MonitorSpecificResponder(Class iface, Object impl, RpcMonitor monitor, ServerNode serverNode) {
        super(iface, impl);

        this.monitor = monitor;
        this.serverNode = serverNode;
    }

    @Override
    public Object respond(Message message, Object request) throws Exception {
        boolean isSuccess = true;
        StatisticsInfo info = new StatisticsInfo();
        long startTime = System.currentTimeMillis();
        try {
            Object result = super.respond(message, request);
            return result;
        } catch (Exception e) {
            isSuccess = false;
            throw e;
        } finally {
            if (monitor != null) {
                long usetime = System.currentTimeMillis() - startTime;
                System.out.println(usetime);
                info.setAvgtime(usetime);
                info.setMaxtime(usetime);
                info.setMintime(usetime);
                if (isSuccess) {
                    info.setSuccess(1L);
                } else {
                    info.setFailure(1L);
                }

                monitor.collect(serverNode, info);
            }
        }
    }

}
