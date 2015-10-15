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
package com.bfd.harpc.test.demo.prop;

import com.bfd.harpc.main.Server;
import com.bfd.harpc.test.EchoServiceImpl;

/**
 * The demo for server by properties file.
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-17
 */
public class ServerDemo {
    /** 配置文件路径，配置说明参考 {@link Server#Server(String[] , Object )} */
    private static final String CONFIG_FILE_PATH = "classpath:demo/demo-server.properties";

    /** 是否保持启动 */
    private static boolean running = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] configs = new String[] { CONFIG_FILE_PATH };
        EchoServiceImpl impl = new EchoServiceImpl();

        try {
            Server server = new Server(configs, impl);
            server.start(); // 启动服务，非阻塞

            synchronized (ServerDemo.class) {
                while (running) {
                    try {
                        ServerDemo.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
