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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 文件路径工具类
 * <p>
 * 
 * @author dsfan
 * @date 2015-8-11
 */
public class PathUtils {

    /**
     * Normalize the path by suppressing sequences like "path/.." and inner
     * simple dots.
     * <p>
     * The result is convenient for path comparison. For other uses, notice that
     * Windows separators ("\") are replaced by simple slashes.
     * 
     * @param path
     *            the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        // ClassLoader.getResource读取打包成jar内的路径。
        if (path.startsWith("jar:file")) {
            path = cleanJarPath(path);
        }

        String pathToUse = StringUtils.replace(path, ResourceConstants.WINDOWS_FOLDER_SEPARATOR.getValue(), ResourceConstants.FOLDER_SEPARATOR.getValue());

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(":");
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            pathToUse = pathToUse.substring(prefixIndex + 1);
        }
        if (pathToUse.startsWith(ResourceConstants.FOLDER_SEPARATOR.getValue())) {
            prefix = prefix + ResourceConstants.FOLDER_SEPARATOR.getValue();
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = StringUtils.split(pathToUse, ResourceConstants.FOLDER_SEPARATOR.getValue());
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (ResourceConstants.CURRENT_PATH.getValue().equals(element)) {
                // Points to current directory - drop it.
            } else if (ResourceConstants.TOP_PATH.getValue().equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top
                    // path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.add(0, ResourceConstants.TOP_PATH.getValue());
        }

        return prefix + StringUtils.join(pathElements, ResourceConstants.FOLDER_SEPARATOR.getValue());
    }

    /**
     * 当打包成jar包后，通过<code>class.getResource</code>或
     * <code>ClassLoader.getResource</code>获取路径会类似这样：
     * jar:file:/c:/myapp/myapp.jar!/path...清理后为：file:/c:/myapp/path...
     * 
     * @param original
     * @return
     */
    public static String cleanJarPath(String original) {
        // jar:file:/tmp/test.jar!/config/dbaccess/mysql
        original = original.substring(4);
        int index = original.indexOf("!");
        String left = original.substring(0, index);
        String right = original.substring(index + 1);
        index = left.lastIndexOf("/");
        left = left.substring(0, index);
        return left + right;

    }

    /**
     * Normalize the path by suppressing sequences like "path/.." and inner
     * simple dots.
     * <p>
     * The result is convenient for path comparison. For other uses, notice that
     * Windows separators ("\") are replaced by simple slashes.
     * 
     * @param originalUrl
     *            the url with original path
     * @return the url with normalized path
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public static URL cleanPath(URL originalUrl) throws MalformedURLException, URISyntaxException {
        String path = originalUrl.toString();
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        URL curl = new URL(cleanPath(path));
        // curl.toURI().getPath()获取正确显示的中文路径。
        return new URL(curl.getProtocol(), curl.getHost(), curl.toURI().getPath());
    }

    /**
     * 获取平台相关的绝对路径。
     * <p>
     * windows下的路径分割符为"\"，Unix*下为"/"。
     * 
     * @param originalUrl
     *            URL
     * @return 绝对路径
     */
    public static String getRealPath(URL originalUrl) {
        String fs = SeparatorUtils.getFileSeparator();
        String file = originalUrl.getFile();
        if (fs.equals("\\")) {
            return file.replace("/", fs);
        }

        return file;
    }

    /**
     * 获取平台相关的绝对路径。
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
     * @return 绝对路径
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String getRealPath(String locationPattern) throws IOException, URISyntaxException {
        URL url = ResourceUtils.loadResource(locationPattern);
        return getRealPath(url);
    }

    /**
     * 获取程序启动的classpath路径（文件系统），如果是jar包则返回jar包所在的目录路径。
     * <p>
     * 
     * @return
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static String getAppDir(Class<?> clazz) {
        File f;
        try {
            f = new File(getCodeLocation(clazz).toURI().getPath());
            return f.isFile() ? f.getParent() : f.getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取代码所在的URL，即class文件所在的路径。
     * <p>
     * <b>NOTE:</b><br />
     * war包返回：file:/path/my-app/calsses/ <br />
     * 打包jar包则返回：file:/path/my-app/my-app.jar.
     * 
     * @return URL
     */
    public static URL getCodeLocation(Class<?> clazz) {
        URL codeLocation = null;
        // If CodeSource didn't work, Class.getResource
        // instead.
        URL r = clazz.getResource("");
        synchronized (r) {
            String s = r.toString();
            Pattern jrare = Pattern.compile("jar:\\s?(.*)!/.*");
            Matcher m = jrare.matcher(s);
            if (m.find()) { // the code is run from a jar file.
                s = m.group(1);
            } else {
                String p = clazz.getPackage().getName().replace('.', '/');
                s = s.substring(0, s.lastIndexOf(p));
            }
            try {
                codeLocation = new URL(s);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return codeLocation;
    }

}
