# -*- coding: utf-8 -*-
import time
import logging
from tutorial import TutorialService # 导入thrift生成的service
from bfd.harpc import client  # 导入harpc 的client
from bfd.harpc.common import config # 导入harpc 配置库

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename='./logs/clientdemo.log',
                    filemode='w')

if __name__ == '__main__':
    # read config file
    conf = config.Config("./etc/demo_client.conf") #读取配置文件
    # setting config  use zk
    # conf = config.Config() # 初始化配置文件
    # conf.set("client", "service", "python_test$EchoService") # 设置服务名，此处和server的相同
    # conf.set("client", "zk_connect_str", "172.18.1.22:2181") # zk 连接地址

    # setting config direct connect， 不通过zk 进行直连的方式连接server
    # conf = config.Config()
    # conf.set("client", "use_zk", "False")
    # conf.set("client", "direct_address", "127.0.0.1:9095") # ip：port 对应server的ip 和 端口
    manager = client.Client(TutorialService.Client, conf)  #TutorialService.Client thrift生成的Client
    proxy_client = manager.create_proxy()
    for i in range(0, 40):
        print proxy_client.echo("hello world!")
        time.sleep(0.1)
    manager.close()
