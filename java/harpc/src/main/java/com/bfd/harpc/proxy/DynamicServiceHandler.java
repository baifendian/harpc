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
package com.bfd.harpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.monitor.RpcMonitor;
import com.bfd.harpc.monitor.StatisticsInfo;

/**
 * 服务实现的动态代理
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-28
 */
public class DynamicServiceHandler implements InvocationHandler {

    /** 实际处理实例 */
    private Object target;

    /** {@link RpcMonitor} */
    private RpcMonitor monitor;

    /** {@link ServerNode} */
    private ServerNode serverNode;

    /**
     * 动态代理绑定实例
     * <p>
     * 
     * @param classLoader
     * @param serviceClass
     * @param target
     * @param rpcMonitor
     * @param serverNode
     * @return 服务处理类代理
     * @throws ClassNotFoundException
     */
    public Object bind(ClassLoader classLoader, Class<?> serviceClass, Object target, RpcMonitor rpcMonitor, ServerNode serverNode) throws ClassNotFoundException {
        this.target = target;
        this.monitor = rpcMonitor;
        this.serverNode = serverNode;
        return Proxy.newProxyInstance(classLoader, new Class[] { serviceClass }, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isError = false;
        StatisticsInfo info = new StatisticsInfo();
        long startTime = System.currentTimeMillis();

        try {
            Object result = method.invoke(target, args);
            return result;
        } catch (Exception e) {
            isError = true;
            throw e;
        } finally {
            if (monitor != null) { // 统计信息
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
    }
}
