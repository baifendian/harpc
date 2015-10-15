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

import java.util.Properties;

/**
 * 分隔符工具类
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-12
 */
public class SeparatorUtils {

    /* system properties to get separators */
    static final Properties PROPERTIES = new Properties(System.getProperties());

    /**
     * get line separator on current platform
     * 
     * @return line separator
     */
    public static String getLineSeparator() {
        return PROPERTIES.getProperty("line.separator");
    }

    /**
     * get path separator on current platform
     * 
     * @return path separator
     */
    public static String getPathSeparator() {
        return PROPERTIES.getProperty("path.separator");
    }

    /**
     * get file separator on current platform
     * 
     * @return path separator
     */
    public static String getFileSeparator() {
        return PROPERTIES.getProperty("file.separator");
    }

}
