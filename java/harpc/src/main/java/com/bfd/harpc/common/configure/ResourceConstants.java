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
package com.bfd.harpc.common.configure;

/**
 * 与资源寻址相关的常量枚举
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-11
 */
public enum ResourceConstants {
    /** 从项目和jar中读取资源的URL前缀 */
    CLASSPATH_ALL_URL_PREFIX("classpath*:"),

    /** 从项目中读取资源的URL前缀 */
    CLASSPATH_URL_PREFIX("classpath:"),

    /** 从文件系统中读取资源的URL前缀 */
    FILE_URL_PREFIX("file:"),

    /** 上层路径 */
    TOP_PATH(".."),

    /** 当前路径 */
    CURRENT_PATH("."),

    /** linux文件夹分隔符 */
    FOLDER_SEPARATOR("/"),

    /** windows文件分隔符 */
    WINDOWS_FOLDER_SEPARATOR("\\");

    /** value */
    private String value;

    /**
     * constructor
     * 
     * @param value
     *            value
     */
    private ResourceConstants(String value) {
        this.value = value;
    }

    /**
     * getter method
     * 
     * @see ResourceConstants#value
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
