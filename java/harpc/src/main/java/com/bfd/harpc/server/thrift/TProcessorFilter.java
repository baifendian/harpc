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

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.monitor.StatisticsInfo;

/**
 * TProcessor过滤器(此类暂时作废)
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-7-3
 */
@Deprecated
public class TProcessorFilter implements TProcessor {

    /** {@link TProcessor} */
    private final TProcessor processor;

    /** {@link RpcMonitor} */
    private final RpcMonitor monitor;

    /** {@link ServerNode} */
    private final ServerNode serverNode;

    /**
     * @param processor
     * @param monitor
     */
    public TProcessorFilter(TProcessor processor, RpcMonitor monitor, ServerNode serverNode) {
        this.processor = processor;
        this.monitor = monitor;
        this.serverNode = serverNode;
    }

    @Override
    public boolean process(TProtocol in, TProtocol out) throws TException {
        boolean isSuccess = false;
        boolean isError = false;
        StatisticsInfo info = new StatisticsInfo();
        long startTime = System.currentTimeMillis();
        try {
            isSuccess = processor.process(in, out);
        } catch (TException e) {
            isError = true;
            throw e;
        } finally {
            if (monitor != null) {
                long usetime = System.currentTimeMillis() - startTime;
                info.setAvgtime(usetime);
                info.setMaxtime(usetime);
                info.setMintime(usetime);
                if (!isError) {
                    info.setSuccess(1L);
                } else {
                    info.setFailure(1L);
                }
                monitor.collect(serverNode, info);
            }
        }
        return isSuccess;
    }
}
