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
import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;

/**
 * 默认注册方式（配置地址）
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-12
 */
public class DefaultRegistry implements IRegistry {

    /** {@link DynamicHostSet} */
    private final DynamicHostSet hostSet = new DynamicHostSet();

    /**
     * @param serverAddress
     */
    public DefaultRegistry(String serverAddress) {
        String[] hostnames = serverAddress.split(";");// "ip:port;ip:port"
        for (String hostname : hostnames) {
            String[] address = hostname.split(":");
            hostSet.addServerInstance(new ServerNode(address[0], Integer.parseInt(address[1])));
        }
    }

    @Override
    public void register(String config) throws RpcException {
        // nothing
    }

    @Override
    public DynamicHostSet findAllService() {
        return hostSet;
    }

    @Override
    public void unregister() {
        // nothing
    }

}
