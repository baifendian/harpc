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
package com.bfd.harpc.registry;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;

/**
 * 注册中心
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-12
 */
public interface IRegistry {

    /**
     * 注册<br>
     * 包括：provider和client
     * <p>
     * 
     * @param config
     *            配置信息
     * @throws RpcException
     */
    void register(String config) throws RpcException;

    /**
     * 获取所以服务
     * <p>
     * 
     * @return
     */
    DynamicHostSet findAllService();

    /**
     * 服务注销
     * <p>
     */
    void unregister();
}
