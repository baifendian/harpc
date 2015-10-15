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

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.ServerNode;

/**
 * TserviceClient链接池工厂(非单例，可重载，建议使用时单例)
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-12
 */
public class TServiceClientPoolFactory<T> extends BaseKeyedPoolableObjectFactory<ServerNode, T> {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** {@link TServiceClientFactory }<{@link TServiceClient}> */
    private final TServiceClientFactory<TServiceClient> clientFactory;

    /** 超时时间 */
    private final int timeout;

    /**
     * @param clientFactory
     * @param timeout
     */
    public TServiceClientPoolFactory(TServiceClientFactory<TServiceClient> clientFactory, int timeout) {
        this.clientFactory = clientFactory;
        this.timeout = timeout;
    }

    /**
     * 生成对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public T makeObject(ServerNode key) throws Exception {
        // 生成client对象
        if (key != null) {
            TSocket tsocket = new TSocket(key.getIp(), key.getPort(), timeout);
            TProtocol protocol = new TBinaryProtocol(tsocket);
            TServiceClient client = clientFactory.getClient(protocol);
            tsocket.open();
            return (T) client;
        }
        LOGGER.error("Not find a vilid server!");
        throw new RpcException("Not find a vilid server!");
    }

    /**
     * 销毁对象
     */
    @Override
    public void destroyObject(ServerNode key, T client) throws Exception {
        TTransport tp = ((TServiceClient) client).getInputProtocol().getTransport();
        tp.close();
    }

    /**
     * 验证链接有效性 <br/>
     * 注意：在服务端口异常关闭的情况下，<code>tp.isOpen()</code>
     * 仍然返回true,所以，正常情况下应该进行socket验证，考虑到服务异常时使用了deadmark算法切换服务，故这里就不需要下面的验证代码了。
     * 
     * <pre>
     * <code>
     *    Socket socket = null;
     *         try {
     *             InetSocketAddress socketAddress = new InetSocketAddress(key.getIp(), key.getPort());
     *             socket = new Socket();
     *             socket.connect(socketAddress, 1000);
     *         } catch (IOException e) {
     *             LOGGER.warn(e.getMessage(), e);
     *             return false;
     *         } finally {
     *             if (socket != null) {
     *                 try {
     *                     socket.close();
     *                 } catch (IOException e) {
     *                     LOGGER.warn(e.getMessage(), e);
     *                 }
     *             }
     *         }
     * </code>
     * </pre>
     */
    @Override
    public boolean validateObject(ServerNode key, T client) {
        TTransport tp = ((TServiceClient) client).getInputProtocol().getTransport();
        return tp.isOpen();
    }

}
