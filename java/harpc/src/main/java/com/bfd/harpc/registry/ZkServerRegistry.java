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
package com.bfd.harpc.registry;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.RpcException;
import com.bfd.harpc.common.Constants;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;

/**
 * 服务端注册（zookeeper方式）
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-13
 */
public class ZkServerRegistry implements IRegistry {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** {@link CuratorFramework} */
    private final CuratorFramework zookeeper;

    /** 服务zookeeper目录 */
    private final String zkPath;

    /** 服务地址 */
    private final String address;

    /** 授权 */
    private final String auth;

    /**
     * @param zookeeper
     * @param zkPath
     * @param address
     * @param auth
     */
    public ZkServerRegistry(CuratorFramework zookeeper, String zkPath, String address, String auth) {
        this.zookeeper = zookeeper;
        this.zkPath = zkPath;
        this.address = address;
        this.auth = auth;
    }

    @Override
    public void register(String config) throws RpcException {
        if (zookeeper.getState() == CuratorFrameworkState.LATENT) {
            zookeeper.start();
            zookeeper.newNamespaceAwareEnsurePath(zkPath);
        }
        addListener(config);
        build(config);
    }

    /**
     * 添加监听器，防止网络异常或者zookeeper挂掉的情况
     * <p>
     * 
     * @param config
     *            配置信息
     */
    private void addListener(final String config) {
        zookeeper.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if (connectionState == ConnectionState.LOST) {// session过期的情况
                    while (true) {
                        try {
                            if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                                if (build(config)) {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 构建节点
     * <p>
     * 
     * @param config
     *            配置信息
     * @throw RpcException
     * @return 是否创建
     */
    private boolean build(String config) throws RpcException {
        // 创建父节点
        createParentsNode();

        // 创建子节点
        StringBuilder pathBuilder = new StringBuilder(zkPath);
        pathBuilder.append(Constants.ZK_SEPARATOR_DEFAULT).append(Constants.ZK_NAMESPACE_SERVERS).append(Constants.ZK_SEPARATOR_DEFAULT).append(address);
        try {
            if (zookeeper.checkExists().forPath(pathBuilder.toString()) == null) {
                zookeeper.create().withMode(CreateMode.EPHEMERAL).forPath(pathBuilder.toString(), config.getBytes(Constants.UTF8));
                return true;
            }
        } catch (Exception e) {
            String message = MessageFormat.format("Create node error in the path : {0}", pathBuilder.toString());
            LOGGER.error(message, e);
            throw new RpcException(message, e);
        }
        return false;
    }

    /**
     * 创建父节点
     * <p>
     * 
     * @throws RpcException
     */
    private void createParentsNode() throws RpcException {
        String parentPath = zkPath + Constants.ZK_SEPARATOR_DEFAULT + Constants.ZK_NAMESPACE_SERVERS;
        try {
            if (zookeeper.checkExists().forPath(parentPath) == null) {
                Id id = new Id("digest", DigestAuthenticationProvider.generateDigest(auth));
                List<ACL> acls = new ArrayList<ACL>(2);
                ACL acl = new ACL(Perms.CREATE, id);// 创建子节点权限，供其他server创建
                Id id2 = new Id("world", "anyone");
                ACL acl2 = new ACL(Perms.READ, id2);// read权限，供管理系统使用
                acls.add(acl);
                acls.add(acl2);
                zookeeper.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).withACL(acls).forPath(parentPath);
            }
        } catch (Exception e) {
            String message = MessageFormat.format("Zookeeper error in the path : {0}", parentPath);
            LOGGER.error(message, e);
            throw new RpcException(message, e);
        }
    }

    @Override
    public DynamicHostSet findAllService() {
        return null;
    }

    @Override
    public void unregister() {
        zookeeper.close();
    }

}
