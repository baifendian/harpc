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
package com.bfd.harpc.test.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.bfd.harpc.common.Constants;
import com.bfd.harpc.common.NetUtils;
import com.bfd.harpc.config.RegistryConfig;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-1
 */
public class ZkTest {
    public static void main(String[] args) {
        try {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setConnectstr("172.18.1.22:2182");
            CuratorFramework zookeeper = registryConfig.obtainZkClient();

            Id id = new Id("digest", DigestAuthenticationProvider.generateDigest("admin:admin123"));
            List<ACL> acls = new ArrayList<ACL>(2);
            ACL acl = new ACL(Perms.CREATE, id);
            Id id2 = new Id("world", "anyone");
            ACL acl2 = new ACL(Perms.READ, id2);
            acls.add(acl);
            acls.add(acl2);

            // zookeeper.setACL().withACL(acls);
            // zookeeper.create().creatingParentsIfNeeded().withACL(acls).forPath("/test");
            // zookeeper.create().withACL(acls).forPath("/test/123",
            // "123".getBytes());
            // zookeeper.delete().forPath("/test/123");

            String address = NetUtils.getLocalHost() + ":0:i_";
            StringBuilder pathBuilder = new StringBuilder("test123");
            // address = "192.168.73.1:0:i_0000000001";
            pathBuilder.append(Constants.ZK_SEPARATOR_DEFAULT).append(Constants.ZK_NAMESPACE_CLIENTS).append(Constants.ZK_SEPARATOR_DEFAULT).append(address);

            // int length = "192.168.1.108:0:i_".length();
            // zookeeper.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(pathBuilder.toString());
            // 创建节点
            for (int i = 0; i < 10; i++) {
                zookeeper.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(pathBuilder.toString());

                // List<String> list =
                // zookeeper.getChildren().forPath("test123/clients");
                // while (list.size() > 10) {
                // Integer min = new Integer(list.get(0).substring(length));
                // for (String s : list) {
                // Integer tempValue = new Integer(s.substring(length));
                // if (tempValue < min)
                // min = tempValue;
                // }
                //
                // zookeeper.delete().forPath("test123/clients" +
                // "/192.168.1.108:0:i_" + String.format("%010d", min));
                // list.remove("192.168.1.108:0:i_" + String.format("%010d",
                // min));
                // }
            }
            Thread.sleep(100000);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
