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
package com.bfd.harpc.test.demo.xml;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The demo of server by xml file.
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-9
 */
public class ServerDemo {

    /** spring配置文件 */
    private static final String SPRING_FILE_PATH = "demo/demo-server.xml";

    /** 是否保持启动 */
    private static boolean running = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            new ClassPathXmlApplicationContext(SPRING_FILE_PATH);

            synchronized (ServerDemo.class) {
                while (running) {
                    try {
                        ServerDemo.class.wait();
                    } catch (Throwable e) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
