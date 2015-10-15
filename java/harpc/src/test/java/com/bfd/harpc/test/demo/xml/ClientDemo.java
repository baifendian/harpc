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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bfd.harpc.test.gen.EchoService.Iface;

/**
 * The demo of client by xml file.
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-9
 */
public class ClientDemo {

    /** spring配置文件 */
    private static final String SPRING_FILE_PATH = "demo/demo-client.xml";

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext(SPRING_FILE_PATH);
            Iface echoService = (Iface) context.getBean("echoService");
            for (int i = 0; i < 100; i++) {
                System.out.println(echoService.echo("world!"));
                Thread.sleep(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
