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
package com.bfd.harpc.pool;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.ServerNode;

/**
 * AvroClient链接池工厂(非单例，可重载，建议使用时单例)
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-16
 */
public class AvroClientPoolFactory<T> extends BaseKeyedPoolableObjectFactory<ServerNode, T> {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** 超时时间 */
    private final int timeout;

    /** 通信的接口 */
    private final Class<?> iface;

    /** {@link ConcurrentMap}< {@link ServerNode} , {@link T}> */
    private final ConcurrentMap<ServerNode, T> transceiverMap = new ConcurrentHashMap<ServerNode, T>();

    /**
     * @param timeout
     * @param iface
     */
    public AvroClientPoolFactory(int timeout, Class<?> iface) {
        this.timeout = timeout;
        this.iface = iface;
    }

    /**
     * 生成对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public T makeObject(ServerNode key) throws Exception {
        // 生成client对象
        if (key != null) {
            T t = null;
            if (transceiverMap.containsKey(key)) {
                t = transceiverMap.get(key);
            } else {
                NettyTransceiver nettyTransceiver = new NettyTransceiver(new InetSocketAddress(key.getIp(), key.getPort()), (long) timeout);
                t = (T) SpecificRequestor.getClient(iface, nettyTransceiver);
            }
            return t;
        }
        LOGGER.error("Not find a vilid server!");
        throw new RpcException("Not find a vilid server!");
    }

}
