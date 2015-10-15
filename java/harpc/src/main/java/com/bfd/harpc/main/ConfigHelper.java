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
package com.bfd.harpc.main;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.configure.PropertiesConfiguration;

/**
 * 配置文件加载帮助类
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-17
 */
class ConfigHelper {

    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHelper.class);

    /**
     * 初始化配置对象
     * <p>
     * 
     * @param configObject
     *            配置文件存储对象
     * @param configPrefix
     *            配置节点前缀
     * @param configuration
     *            {@link PropertiesConfiguration}
     * @throws RpcException
     */
    public static void initConfig(Object configObject, String configPrefix, PropertiesConfiguration configuration) throws RpcException {
        Method[] methods = configObject.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().length() > 3 && method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                String attribute = method.getName().substring(3);
                char ch = attribute.charAt(0);
                attribute = Character.toLowerCase(ch) + attribute.substring(1);
                String value = configuration.getProperty(configPrefix + attribute, "");

                try {
                    if (StringUtils.isNotEmpty(value)) {
                        Type type = method.getParameterTypes()[0];
                        if (type == boolean.class) {
                            method.invoke(configObject, Boolean.valueOf(value));
                        } else if (type == int.class) {
                            method.invoke(configObject, Integer.valueOf(value));
                        } else if (type == long.class) {
                            method.invoke(configObject, Long.valueOf(value));
                        } else {
                            method.invoke(configObject, value);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Init config error", e);
                    throw new RpcException(RpcException.CONFIG_EXCEPTION, e);
                }
            }
        }
    }
}
