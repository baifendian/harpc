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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ip和port的获取方法
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-8
 */
public class NetUtils {

    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);

    /** 本机回送地址 */
    public static final String LOCALHOST = "127.0.0.1";

    /** 任意网络地址 */
    public static final String ANYHOST = "0.0.0.0";

    /** 随机port起始地址 */
    private static final int RND_PORT_START = 30000;

    /** 随机port的范围 */
    private static final int RND_PORT_RANGE = 10000;

    /** {@link Random} */
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * private constructor
     */
    private NetUtils() {
        super();
    }

    /**
     * 获取随机port
     * <p>
     * 
     * @return port
     */
    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }

    /**
     * 获取有效port
     * <p>
     * 
     * @return port
     */
    public static int getAvailablePort() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 获取有效port
     * <p>
     * 
     * @param port
     * @return port
     */
    public static int getAvailablePort(int port) {
        if (port <= 0) {
            return getAvailablePort();
        }
        for (int i = port; i < MAX_PORT; i++) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(i);
                return i;
            } catch (IOException e) {
                // continue
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return port;
    }

    /** 最小port */
    private static final int MIN_PORT = 0;

    /** 最大port */
    private static final int MAX_PORT = 65535;

    /**
     * port是否合法
     * <p>
     * 
     * @param port
     * @return 是否合法
     */
    public static boolean isInvalidPort(int port) {
        return port > MIN_PORT || port <= MAX_PORT;
    }

    /** 本机ip正则模式 */
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    /**
     * 判断是否本机
     * <p>
     * 
     * @param host
     * @return 是否本机
     */
    public static boolean isLocalHost(String host) {
        return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase("localhost"));
    }

    /**
     * 判断是否任意网络
     * <p>
     * 
     * @param host
     * @return 是否任意网络
     */
    public static boolean isAnyHost(String host) {
        return "0.0.0.0".equals(host);
    }

    /**
     * 判断本地ip是否有效
     * <p>
     * 
     * @param host
     * @return 是否有效本机
     */
    public static boolean isInvalidLocalHost(String host) {
        return host == null || host.length() == 0 || host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0") || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    /**
     * 判断本机ip是否合法
     * <p>
     * 
     * @param host
     * @return 是否合法
     */
    public static boolean isValidLocalHost(String host) {
        return !isInvalidLocalHost(host);
    }

    /**
     * 获取本机socket地址
     * <p>
     * 
     * @param host
     * @param port
     * @return {@link InetSocketAddress}
     */
    public static InetSocketAddress getLocalSocketAddress(String host, int port) {
        return isInvalidLocalHost(host) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
    }

    /** ip正则模式 */
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    /**
     * 验证ip地址是否有效
     * <p>
     * 
     * @param address
     * @return 是否有效
     */
    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress())
            return false;
        String name = address.getHostAddress();
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
    }

    /**
     * 获取本机ip
     * <p>
     * 
     * @return 本机ip地址
     */
    public static String getLocalHost() {
        InetAddress address = getLocalAddress();
        return address == null ? LOCALHOST : address.getHostAddress();
    }

    /** {@link InetAddress} */
    private static volatile InetAddress LOCAL_ADDRESS = null;

    /**
     * 获取本机网卡ip
     * <p>
     * 
     * @return 本地网卡IP
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null)
            return LOCAL_ADDRESS;
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    /**
     * 遍历本地网卡，返回第一个合理的IP。
     * <p>
     * 
     * @return
     */
    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        LOGGER.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

}