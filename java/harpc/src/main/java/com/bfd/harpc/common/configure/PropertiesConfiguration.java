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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * properties配置文件读取工具类。
 * <p>
 * 支持两种获取配置的方法：<br />
 * 1.集中读取：合并所有配置文件中的项，对于相同key的配置后面的会覆盖前面的。静态方法{@link #getValue(String, boolean)}
 * 及其重载方法用来获取合并后的相应key的value。 <br />
 * 2.分别读取：指定读取某个文件的某个项。{@link #get(Object)}获取关联某个文件的
 * {@link PropertiesConfiguration}对象,{@link #getString(String)},
 * {@link #getInt(String, int)},{@link #getLong(String, long)},
 * {@link #getDouble(String, double)},{@link #getBoolean(String, Boolean)}
 * 等方法用来获取特定文件中相应key的value。 <b>NOTE：</b> 文件格式最好为UTF-8
 * 
 * @author : dsfan
 * @date : 2015-8-11
 */
public class PropertiesConfiguration extends Properties {
    /** Serial version UID */
    private static final long serialVersionUID = 5889693716376287459L;

    /** properties的Map */
    private static Map<String, PropertiesConfiguration> properties = new ConcurrentHashMap<String, PropertiesConfiguration>();

    /** 合并后的properties */
    private static final PropertiesConfiguration ENTIRE = new PropertiesConfiguration();

    /**
     * private constructor
     */
    private PropertiesConfiguration() {
    }

    /**
     * 获取{@link PropertiesConfiguration}.
     * 
     * @param locationPattern
     *            0. 路径寻址前缀请参见{@link ResourceConstants}<br />
     *            1. 使用file，classpath和classpath*做路径开头<br />
     *            2. classpath寻址项目中的文件<br />
     *            3. classpath*既寻址项目，也寻址jar包中的文件<br />
     *            4. file寻址文件系统中的文件:如：D:\\config.properties,/etc/config.
     *            properties<br />
     *            5. 默认是classpath<br />
     *            6. 例如：classpath*:log/log4j.xml;file:/home/ydhl/
     *            abc.sh;classpath:log/log4j.xml
     * @return {@link PropertiesConfiguration}
     */
    public static PropertiesConfiguration newInstance(String locationPattern) {
        String key = getKey(locationPattern);
        if (!properties.containsKey(key) || properties.get(key) == null) {
            PropertiesConfiguration pc = makeInstance(locationPattern);
            properties.put(key, pc);
        }

        return properties.get(key);
    }

    /**
     * 加载配置文件。
     * 
     * @param locationPatterns
     *            可以一次加载多个配置文件。{@link PropertiesConfiguration#newInstance}
     */
    public static void load(String[] locationPatterns) {
        for (String pattern : locationPatterns) {
            String key = getKey(pattern);
            if (!properties.containsKey(key)) {
                PropertiesConfiguration pc = makeInstance(pattern);
                properties.put(key, pc);
            }
        }
    }

    /**
     * 清空已经加载的配置。
     */
    public static void clean() {
        ENTIRE.clear();
        for (PropertiesConfiguration conf : properties.values()) {
            conf.clear();
        }
    }

    /**
     * 合并配置
     * <p>
     * 
     * @param target
     */
    private static void merge(PropertiesConfiguration target) {
        for (Map.Entry<Object, Object> entry : target.entrySet()) {
            ENTIRE.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取已经加载的配置文件。
     * 
     * @param name
     *            {@link PropertiesConfiguration#newInstance}
     * @return
     * @throws IllegalAccessException
     */
    public static PropertiesConfiguration get(String name) throws IllegalAccessException {
        if (!properties.containsKey(name)) {
            throw new IllegalAccessException("[" + name + "]没有被加载。");
        }
        return properties.get(name);
    }

    private static PropertiesConfiguration makeInstance(String name) {
        try {
            PropertiesConfiguration pro = new PropertiesConfiguration(name);
            merge(pro);
            return pro;
        } catch (Exception e) {
            // convert a checked to a runtime exception
            throw new RuntimeException(e);
        }
    }

    private PropertiesConfiguration(String locationPattern) throws IOException, URISyntaxException {
        String path = "";
        InputStreamReader is = null;
        try {
            path = PathUtils.getRealPath(locationPattern);
            is = new InputStreamReader(new FileInputStream(path), "UTF-8");
            load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /***
     * 获取String类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 返回属性的value，若找不到key，返回默认值
     * @throws IOException
     */
    public String getString(String propertyName, String defaultVal) {
        String value = getString(propertyName);
        if (value == null) {
            return defaultVal;
        } else {
            return value;
        }
    }

    /***
     * 获取int类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 返回属性的value，若找不到key，返回默认值
     * @throws IOException
     */
    public int getInt(String propertyName, int defaultVal) {
        int value;
        try {
            String tmp = getString(propertyName);
            if (tmp == null) {
                return defaultVal;
            }

            value = Integer.parseInt(tmp);
            return value;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /***
     * 获取double类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 返回属性的value，若找不到key，返回默认值
     * @throws IOException
     */
    public double getDouble(String propertyName, double defaultVal) {
        double value;
        try {
            String tmp = getString(propertyName);
            if (tmp == null) {
                return defaultVal;
            }

            value = Double.parseDouble(tmp);
            return value;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /***
     * 获取long类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 返回属性的value，若找不到key，返回默认值
     * @throws IOException
     */
    public long getLong(String propertyName, long defaultVal) {
        long value;
        try {
            String tmp = getString(propertyName);
            if (tmp == null) {
                return defaultVal;
            }

            value = Long.parseLong(tmp);
            return value;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /***
     * 获取Boolean类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 返回属性的value，若找不到key，返回默认值
     * @throws IOException
     */
    public boolean getBoolean(String propertyName, Boolean defaultVal) {
        boolean value;
        try {
            String tmp = getString(propertyName);
            if (tmp == null) {
                return defaultVal;
            }

            value = Boolean.parseBoolean(tmp);
            return value;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private String getString(String propertyName) {
        return this.getProperty(propertyName);
    }

    /**
     * classpath:configuration/config.properties 或classpath:config.properties或D
     * :\\config.properties||/etc/config.properties
     */
    private static String getKey(String locationPattern) {
        URL url = null;
        try {
            url = ResourceUtils.loadResource(locationPattern);
            File file = new File(url.getFile());
            return file.getName().substring(0, file.getName().indexOf("."));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取String类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 属性的value
     */
    public static String getValue(String propertyName, String defaultVal) {
        return ENTIRE.getString(propertyName, defaultVal);
    }

    /**
     * 获取int类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 属性的value
     */
    public static int getValue(String propertyName, int defaultVal) {
        return ENTIRE.getInt(propertyName, defaultVal);
    }

    /**
     * 获取long类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 属性的value
     */
    public static long getValue(String propertyName, long defaultVal) {
        return ENTIRE.getLong(propertyName, defaultVal);
    }

    /**
     * 获取double类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 属性的value
     */
    public static double getValue(String propertyName, double defaultVal) {
        return ENTIRE.getDouble(propertyName, defaultVal);
    }

    /**
     * 获取boolean类型的配置。
     * 
     * @param propertyName
     *            属性的key
     * @param defaultVal
     *            默认值
     * @return 属性的value
     */
    public static boolean getValue(String propertyName, boolean defaultVal) {
        return ENTIRE.getBoolean(propertyName, defaultVal);
    }

}
