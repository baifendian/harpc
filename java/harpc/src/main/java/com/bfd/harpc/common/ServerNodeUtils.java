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
package com.bfd.harpc.common;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-13
 */
public class ServerNodeUtils {

    /** 默认权重 */
    private static final int DEFAULT_WEIGHT = 1;

    /**
     * private constructor
     */
    private ServerNodeUtils() {
        super();
    }

    /**
     * 服务地址转换为ServerNode列表
     * <p>
     * 
     * @param address
     * @return {@link List<ServerNode>}
     */
    public static List<ServerNode> transfer(String address) {
        String[] hostname = address.split(":");
        int weight = DEFAULT_WEIGHT;
        if (hostname.length == 3) {
            weight = Integer.parseInt(hostname[2]);
        }
        String ip = hostname[0];
        Integer port = Integer.valueOf(hostname[1]);
        List<ServerNode> result = new ArrayList<ServerNode>();
        for (int i = 0; i < weight; i++) {
            result.add(new ServerNode(ip, port));
        }
        return result;
    }

}
