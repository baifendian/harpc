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
package com.bfd.harpc.test.heartbeat;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.heartbeat.HeartBeatManager;
import com.bfd.harpc.loadbalance.common.DynamicHostSet;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-17
 */
public class HeartBeatTest {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) {
        DynamicHostSet dynamicHostSet = new DynamicHostSet();
        dynamicHostSet.addLiveInstance(new ServerNode("127.0.0.1", 80));
        dynamicHostSet.addLiveInstance(new ServerNode("127.0.0.1", 90));
        dynamicHostSet.addLiveInstance(new ServerNode("127.0.0.1", 2181));
        HeartBeatManager manager = new HeartBeatManager(dynamicHostSet, 1000, 2000, 3, 1000, null);
        manager.startHeatbeatTimer();

        while (true) {
            System.out.println("lives:" + dynamicHostSet.getLives());
            System.out.println("deads:" + dynamicHostSet.getDeads());
            // System.out.println(manager.getDeads());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
