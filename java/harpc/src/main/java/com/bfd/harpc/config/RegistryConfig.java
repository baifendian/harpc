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
package com.bfd.harpc.config;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.Constants;

import java.io.UnsupportedEncodingException;

/**
 * 注册中心配置
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-19
 */
public class RegistryConfig implements IConfigCheck {

    /** javabean的id */
    private String id;

    /** 链接字符串 */
    private String connectstr;

    /** 会话超时时间 */
    private int timeout = 3000;

    /** 重试次数，默认重试为1次 */
    private int retry = 1;

    /** 共享一个zk链接，默认为true */
    private boolean singleton = true;

    /** 全局path前缀,常用来区分不同的应用 */
    private String namespace = Constants.ZK_NAMESPACE_ROOT;

    /** 授权字符串(server端配置，client端不用设置) */
    private String auth;

    /** {@link CuratorFramework} */
    private CuratorFramework zkClient;

    /**
     * 获取zkClient
     * <p>
     * 
     * @return {@link CuratorFramework}
     * @throws Exception
     */
    public CuratorFramework obtainZkClient() throws RpcException {
        check(); // 配置检查
        if (singleton) {
            if (zkClient == null) {
                zkClient = create();
                zkClient.start();
            }
            return zkClient;
        }
        zkClient = create();
        return zkClient;
    }

    /**
     * 创建CuratorFramework实例
     * <p>
     * 
     * @return {@link CuratorFramework}
     * @throws Exception
     */
    private CuratorFramework create() throws RpcException {
        return create(connectstr, timeout, namespace, retry);
    }

    /**
     * 创建CuratorFramework实例
     * <p>
     * 
     * @param connectString
     * @param sessionTimeout
     * @param namespace
     * @return {@link CuratorFramework}
     */
    private CuratorFramework create(String connectString, Integer sessionTimeout, String namespace, int retry){
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        if (StringUtils.isNotEmpty(auth)) {
            try {
                builder.authorization("digest", auth.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                //Assert never throw
                e.printStackTrace();
            }
        }
        return builder.connectString(connectString)
                      .sessionTimeoutMs(sessionTimeout)
                      .connectionTimeoutMs(sessionTimeout)
                      .namespace(namespace)
                      .retryPolicy(new ExponentialBackoffRetry(1000,retry))
                      .defaultData(null).build();
    }

    /**
     * 关闭链接
     * <p>
     */
    public void close() {
        if (zkClient != null) {
            zkClient.close();
        }
    }

    @Override
    public void check() throws RpcException {
        if (StringUtils.isEmpty(connectstr)) {
            throw new RpcException(RpcException.CONFIG_EXCEPTION, "The params 'connectstr' cannot empty!");
        }
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#id
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#id
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#connectstr
     * @return the connectstr
     */
    public String getConnectstr() {
        return connectstr;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#connectstr
     * @param connectstr
     *            the connectstr to set
     */
    public void setConnectstr(String connectstr) {
        this.connectstr = connectstr;
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#timeout
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#timeout
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#retry
     * @return the retry
     */
    public int getRetry() {
        return retry;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#retry
     * @param retry
     *            the retry to set
     */
    public void setRetry(int retry) {
        this.retry = retry;
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#singleton
     * @return the singleton
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#singleton
     * @param singleton
     *            the singleton to set
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#namespace
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#namespace
     * @param namespace
     *            the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * getter method
     * 
     * @see RegistryConfig#auth
     * @return the auth
     */
    public String getAuth() {
        return auth;
    }

    /**
     * setter method
     * 
     * @see RegistryConfig#auth
     * @param auth
     *            the auth to set
     */
    public void setAuth(String auth) {
        this.auth = auth;
    }

}
