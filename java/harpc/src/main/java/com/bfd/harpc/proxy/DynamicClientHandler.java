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

import com.bfd.harpc.client.Invoker;

/**
 * client动态代理
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-8
 */
public class DynamicClientHandler implements InvocationHandler {

    /** {@link Invoker} */
    private final Invoker invoker;

    /**
     * @param invoker
     */
    public DynamicClientHandler(Invoker invoker) {
        this.invoker = invoker;
    }

    /**
     * 动态代理绑定实例
     * <p>
     * 
     * @param classLoader
     *            {@link ClassLoader}
     * @param service
     *            {@link Class}
     * @return {@link Object}
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public <T> T bind(ClassLoader classLoader, Class<?> serviceClass) throws ClassNotFoundException {
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { serviceClass }, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(method, args);
    }

}
