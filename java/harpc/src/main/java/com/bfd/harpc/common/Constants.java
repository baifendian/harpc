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

/**
 * 常量类，用于共享常见的一些常量，如:utf-8等
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-11
 */
public class Constants {

    /** utf-8 */
    public static final String UTF8 = "utf-8";

    /** zookeeper根目录 */
    public static final String ZK_NAMESPACE_ROOT = "harpc";

    /** zookeeper目录分割符 */
    public static final String ZK_SEPARATOR_DEFAULT = "/";

    /** servers子目录 */
    public static final String ZK_NAMESPACE_SERVERS = "servers";

    /** clients子目录 */
    public static final String ZK_NAMESPACE_CLIENTS = "clients";

    /** configs子目录 */
    public static final String ZK_NAMESPACE_CONFIGS = "configs";

    /** statistics子目录 */
    public static final String ZK_NAMESPACE_STATISTICS = "statistics";

    /** zookeeper中使用时间戳作目录的格式 */
    public static final String ZK_TIME_NODE_FORMAT = "yyyyMMddHHmmss";

    /** zookeeper中总计节点名称 */
    public static final String ZK_NAMESPACE_TOTAL = "total";

    /** zookeeper中详细节点名称 */
    public static final String ZK_NAMESPACE_DETAIL = "detail";

    /**
     * private constructor
     */
    private Constants() {
        super();
    }

}
