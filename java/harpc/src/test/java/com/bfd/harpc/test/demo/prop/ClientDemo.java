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

import com.bfd.harpc.main.Client;
import com.bfd.harpc.test.gen.EchoService.Iface;

/**
 * The demo for client by properties file.
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-8-17
 */
public class ClientDemo {
    /** 配置文件路径，配置说明参考 {@link Client#Client(String[])} */
    private static final String CONFIG_FILE_PATH = "classpath:demo/demo-client.properties";

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] configs = new String[] { CONFIG_FILE_PATH };

        try {
            Client<Iface> client = new Client<Iface>(configs);
            // 注意:代理内部已经使用连接池，所以这里只需要创建一个实例，多线程共享；特殊情况下，可以允许创建多个实例，
            // 但严禁每次调用前都创建一个实例。
            Iface echoIface = client.createProxy();

            for (int i = 0; i < 1000; i++) {
                try {
                    System.out.println(echoIface.echo("world"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
