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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

/**
 * 文件路径寻址工具<br />
 * 开发者可以从{@link ResourceConstants}中查找支持的三种路径寻址前缀<br />
 * 举例：ResourceUtils.loadResource("classpath*:log/log4j.xml");
 * <p>
 * 
 * @author dsfan
 * @date 2015-8-11
 */
public class ResourceUtils {
    /** 加锁工具 */
    private static final Object SYS_OBJECT = new Object();

    /** {@link ClassLoader} */
    private static ClassLoader classLoader;

    /**
     * 从项目，jar或文件系统中读取指定路径的文件<br />
     * 与loadResources()区别是本方法在有返回值时默认只返回一条记录，其余丢弃
     * <p>
     * 
     * @param locationPattern
     * <br/>
     *            0. 路径寻址前缀请参见{@link ResourceConstants}<br />
     *            1. 使用file，classpath和classpath*做路径开头<br />
     *            2. classpath寻址项目中的文件<br />
     *            3. classpath*既寻址项目，也寻址jar包中的文件<br />
     *            4. file寻址文件系统中的文件<br />
     *            5. 默认是classpath 6.
     *            例如：classpath*:log/log4j.xml;file:/home/ydhl/
     *            abc.sh;classpath:log/log4j.xml
     * @return 以URL返回结果
     * @throws IOException
     * @throws URISyntaxException
     */
    public static URL loadResource(String locationPattern) throws IOException, URISyntaxException {
        URL[] urlArray = loadResources(locationPattern);

        return ArrayUtils.isEmpty(urlArray) ? null : urlArray[0];
    }

    /**
     * 从项目，jar或文件系统中读取指定路径的文件<br />
     * <p>
     * 
     * @param locationPattern
     * <br/>
     *            0. 路径寻址前缀请参见{@link ResourceConstants}<br />
     *            1. 使用file，classpath和classpath*做路径开头<br />
     *            2. classpath寻址项目中的文件<br />
     *            3. classpath*既寻址项目，也寻址jar包中的文件<br />
     *            4. file寻址文件系统中的文件<br />
     *            5. 默认是classpath 6.
     *            例如：classpath*:log/log4j.xml；file:/home/ydhl/
     *            abc.sh；classpath:log/log4j.xml
     * @return 以URL返回结果
     * @throws IOException
     * @throws URISyntaxException
     *             中文路径支持
     */
    public static URL[] loadResources(String locationPattern) throws IOException, URISyntaxException {
        if (locationPattern.startsWith(ResourceConstants.CLASSPATH_ALL_URL_PREFIX.getValue())) {
            return load1(locationPattern);
        } else if (locationPattern.startsWith(ResourceConstants.CLASSPATH_URL_PREFIX.getValue())) {
            return load2(locationPattern);
        } else if (locationPattern.startsWith(ResourceConstants.FILE_URL_PREFIX.getValue())) {
            return load3(locationPattern);
        } else {
            // 默认为文件系统路径。
            locationPattern = "file:" + locationPattern;
            return ResourceUtils.loadResources(locationPattern);
        }
    }

    private static URL[] load1(String locationPattern) throws IOException, URISyntaxException {
        String location = locationPattern.substring(ResourceConstants.CLASSPATH_ALL_URL_PREFIX.getValue().length());
        if (location.startsWith(ResourceConstants.FOLDER_SEPARATOR.getValue())) {
            location = location.substring(1);
        }

        Enumeration<URL> resourceUrls = getDefaultClassLoader().getResources(location);
        Set<URL> result = new LinkedHashSet<URL>(16);
        while (resourceUrls.hasMoreElements()) {
            URL url = resourceUrls.nextElement();
            result.add(PathUtils.cleanPath(url));
        }
        return result.toArray(new URL[result.size()]);
    }

    private static URL[] load2(String locationPattern) throws URISyntaxException, IOException {
        String location = locationPattern.substring(ResourceConstants.CLASSPATH_URL_PREFIX.getValue().length());
        if (location.startsWith(ResourceConstants.FOLDER_SEPARATOR.getValue())) {
            location = location.substring(1);
        }

        String cleanPath = PathUtils.cleanPath(location);

        // a single resource with the given name
        URL url = getDefaultClassLoader().getResource(cleanPath);
        // if (url == null) {
        // throw new UnsupportedOperationException(cleanPath);
        // }
        return url == null ? null : new URL[] { PathUtils.cleanPath(url) };
    }

    private static URL[] load3(String locationPattern) throws MalformedURLException {

        // a single resource with the given name
        URL url = new URL(locationPattern);
        return new File(url.getFile()).exists() ? new URL[] { url } : null;
    }

    /**
     * 获取运行时classloader，首选线程上下文classloader，其次选择类classloader
     * <p>
     * 
     * @return {@link ClassLoader}
     */
    public static ClassLoader getDefaultClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }

        synchronized (SYS_OBJECT) {
            if (classLoader == null) {
                ClassLoader tempClassLoader = null;
                try {
                    tempClassLoader = Thread.currentThread().getContextClassLoader();
                } catch (Exception ex) {
                    // Cannot access thread context ClassLoader - falling back
                    // to system class loader...
                }

                if (tempClassLoader == null) {
                    // No thread context class loader -> use class loader of
                    // this class.
                    tempClassLoader = ResourceUtils.class.getClassLoader();
                }

                classLoader = tempClassLoader;
            }
        }

        return classLoader;
    }
}