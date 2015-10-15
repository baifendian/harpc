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
package com.bfd.harpc.loadbalance.common;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bfd.harpc.common.ServerNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * 动态服务主机集合
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-10
 */
public class DynamicHostSet implements DynamicSet<ServerNode> {

    /** LOGGER */
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** 所有的{@link ServerNode} */
    private final Set<ServerNode> all = Sets.newHashSet();

    /** 可访问的{@link ServerNode} */
    private final Set<ServerNode> lives = Sets.newHashSet();

    /** 不可访问的{@link ServerNode} */
    private final Set<ServerNode> deads = Sets.newHashSet();

    /** {@link HostChangeMonitor} */
    private HostChangeMonitor<ServerNode> monitor;

    /**
     * add a server instance.
     * 
     * @param serverNode
     *            {@link ServerNode}
     */
    public synchronized void addServerInstance(ServerNode serverNode) {
        if (!lives.contains(serverNode)) {
            LOGGER.warn("add {} to lives set", serverNode);
            lives.add(serverNode);
            LOGGER.warn("add {} to all set", serverNode);
            all.add(serverNode);
            onChange();
        }
    }

    /**
     * add a live instance for heartbeat.
     * <p>
     * 
     * @param serverNode
     *            {@link ServerNode}
     */
    public synchronized void addLiveInstance(ServerNode serverNode) {
        if (all.contains(serverNode) && deads.contains(serverNode)) {
            lives.add(serverNode);
            LOGGER.warn("add {} to lives set", serverNode);
            deads.remove(serverNode);
            LOGGER.warn("remove {} from deads set", serverNode);
            onChange();
        }
    }

    /**
     * add dead instance
     * 
     * @param serverNode
     *            {@link ServerNode}
     */
    public synchronized void addDeadInstance(ServerNode serverNode) {
        if (all.contains(serverNode) && lives.contains(serverNode)) {
            deads.add(serverNode);
            LOGGER.warn("add {} to deads set", serverNode);
            lives.remove(serverNode);
            LOGGER.warn("remove {} from lives set", serverNode);
            if (lives.size() == 0) {
                adjustAll();
            }
            onChange();
        }
    }

    /**
     * 重新调整
     * <p>
     */
    private void adjustAll() {
        for (ServerNode serverNode : all) {
            lives.add(serverNode);
        }
        deads.clear();
    }

    /**
     * replace all hosts with new
     * 
     * @param hosts
     */
    public synchronized void replaceWithList(Collection<ServerNode> hosts) {
        String hostSetString = toString();
        // all替换为最新的集合
        all.clear();
        all.addAll(hosts);

        // deads取与最新集合的交集
        deads.retainAll(hosts);

        // lives取all和deads的差集
        lives.clear();
        lives.addAll(hosts);
        lives.removeAll(deads);

        LOGGER.warn("replace " + hostSetString + " with " + toString());
        onChange();
    }

    @Override
    public void monitor(HostChangeMonitor<ServerNode> monitor) {
        this.monitor = monitor;
        this.onChange();
    }

    /**
     * notify monitor
     * <p>
     */
    private void onChange() {
        if (monitor != null) {
            monitor.onChange(ImmutableSet.copyOf(lives));
        }
    }

    @Override
    public String toString() {
        return "[all=" + all + ", lives=" + lives + ", deads=" + deads + "]";
    }

    /**
     * getter method
     * 
     * @see DynamicHostSet#all
     * @return the all
     */
    public Set<ServerNode> getAll() {
        return all;
    }

    /**
     * getter method
     * 
     * @see DynamicHostSet#lives
     * @return the lives
     */
    public Set<ServerNode> getLives() {
        return lives;
    }

    /**
     * getter method
     * 
     * @see DynamicHostSet#deads
     * @return the deads
     */
    public Set<ServerNode> getDeads() {
        return deads;
    }
}
