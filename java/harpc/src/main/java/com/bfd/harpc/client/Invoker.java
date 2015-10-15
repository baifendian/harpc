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
package com.bfd.harpc.client;

import java.lang.reflect.Method;

import com.bfd.harpc.RpcException;

/**
 * 调用者（用于实际发起请求）
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-3
 */
public interface Invoker {
    /**
     * 调用
     * <p>
     * 
     * @param method
     * @param args
     * @return result
     * @throws RpcException
     */
    Object invoke(Method method, Object[] args) throws RpcException;
}
