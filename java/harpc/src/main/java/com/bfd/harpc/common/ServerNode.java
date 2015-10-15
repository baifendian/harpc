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

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Objects;

/**
 * 服务节点
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-8
 */
public class ServerNode {

    /** 服务ip地址 */
    private String ip;

    /** 服务端口 */
    private int port;

    /** 扩展标识（用于client端） */
    private String ext;

    /**
     * @param ip
     * @param port
     */
    public ServerNode(String ip, int port) {
        super();
        this.ip = ip;
        this.port = port;
    }

    /**
     * @param ip
     * @param port
     * @param ext
     */
    public ServerNode(String ip, int port, String ext) {
        super();
        this.ip = ip;
        this.port = port;
        this.ext = ext;
    }

    /**
     * 获取ServerNode
     * <p>
     * 
     * @param host
     * @param port
     * @return {@link ServerNode}
     */
    public static ServerNode fromParts(String host, int port) {
        return new ServerNode(host, port);
    }

    /**
     * 生成服务地址<br>
     * Server端：ip:port <br>
     * Client端：ip:port:i_节点序列号 <br>
     * <p>
     * 
     * @return ip:port
     */
    public String genAddress() {
        return ip + ":" + port + (StringUtils.isEmpty(ext) ? "" : (":" + ext));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ServerNode) {
            ServerNode that = (ServerNode) other;
            return Objects.equal(this.ip, that.ip) && this.port == that.port && Objects.equal(this.ext, that.ext);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ip, port, ext);
    }

    @Override
    public String toString() {
        return "ServerNode [ip=" + ip + ", port=" + port + "]";
    }

    /**
     * getter method
     * 
     * @see ServerNode#ip
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * setter method
     * 
     * @see ServerNode#ip
     * @param ip
     *            the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * getter method
     * 
     * @see ServerNode#port
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * setter method
     * 
     * @see ServerNode#port
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * getter method
     * 
     * @see ServerNode#ext
     * @return the ext
     */
    public String getExt() {
        return ext;
    }

    /**
     * setter method
     * 
     * @see ServerNode#ext
     * @param ext
     *            the ext to set
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

}
