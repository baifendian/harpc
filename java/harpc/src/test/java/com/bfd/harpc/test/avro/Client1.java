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
package com.bfd.harpc.test.avro;

import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import com.bfd.harpc.test.gen.MessageProtocol;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-16
 */
public class Client1 {

    private String host = null;

    private int port = 0;

    private int count = 0;

    public Client1(String host, int port, int count) {
        this.host = host;
        this.port = port;
        this.count = count;
    }

    public long sendMessage() throws Exception {
        NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(host, port));
        final MessageProtocol proxy = SpecificRequestor.getClient(MessageProtocol.class, client);

        long start = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            proxy.sendMessage("world");
            if (i % 10000 == 0) {
                System.out.println("Tps:" + (int) (1000 / ((System.currentTimeMillis() - start) / (double) i)));
            }
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) + " ms");

        return end - start;
    }

    public long run() {
        long res = 0;
        try {
            res = sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        new Client1("127.0.0.1", 9090, 1000000).run();
    }
}
