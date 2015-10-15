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
package com.bfd.harpc.test.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

import com.bfd.harpc.test.gen.EchoService.Client;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-10
 */
public class ThriftMain {
    public static void main(String[] args) {
        TSocket transport = new TSocket("127.0.0.1", 19091);
        TProtocol protocol = new TBinaryProtocol(transport);
        Client client = new Client(protocol);

        try {
            transport.open();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                client.echo("hello world!");
            }
            System.out.println(System.currentTimeMillis() - startTime);
        } catch (TException e) {
            System.err.println(e.getMessage());
        }
    }
}
