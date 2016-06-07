package com.bfd.harpc.test.performence;

import com.bfd.harpc.config.ClientConfig;
import com.bfd.harpc.config.RegistryConfig;
import com.bfd.harpc.test.gen.EchoService;
import com.bfd.harpc.test.gen.MessageProtocol;
import org.apache.thrift.TException;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xwarrior on 16/6/7.
 */
public class ClientPerformenceTest {
    public static void main(String[] args) throws Exception {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setConnectstr("127.0.0.1:2181");
        //registryConfig.setConnectstr("172.18.1.22:2181");

        String iface = EchoService.Iface.class.getName();
        // String iface = MessageProtocol.class.getName();
        ClientConfig<MessageProtocol> clientConfig = new ClientConfig<>();
        clientConfig.setService("com.bfd.harpc.test$EchoService");
        clientConfig.setIface(iface);
        clientConfig.setProtocol("thrift");
        // clientConfig.setProtocol("avro");
        clientConfig.setHeartbeat(2000);
        clientConfig.setMonitor(true);
        clientConfig.setInterval(60);
        clientConfig.setRetry(0);

        final EchoService.Iface echo = (EchoService.Iface) clientConfig.createProxy(registryConfig);

        long startTime = System.currentTimeMillis();


        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                50,50,100,TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                      echo.echo("world!");
                } catch (TException e) {
                    e.printStackTrace();
                }
            }
        };

        for(int i=0; i < 10000000;i++) {
            pool.submit(r);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE,TimeUnit.DAYS);

        long used = System.currentTimeMillis() - startTime;
        System.out.println("10000000.0 times query used millseconds:" + used + ",avg qps:" + (   10000000.0 / (used / 1000.0 ) ) );

    }
}
