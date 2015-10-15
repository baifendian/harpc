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

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.specific.SpecificResponder;

import com.bfd.harpc.common.ServerNode;
import com.bfd.harpc.server.avro.AvroRpcPlugin;
import com.bfd.harpc.test.gen.MessageProtocol;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-15
 */
public class Server1 {

    public static void main(String[] args) {
        int port = 9090;

        try {
            Responder responder = new SpecificResponder(MessageProtocol.class, new MessageProtocolImpl());
            responder.addRPCPlugin(new AvroRpcPlugin(null, new ServerNode("", port)));

            NettyServer server = new NettyServer(responder, new InetSocketAddress(port));
            server.start();

            // server.join();
            // server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
