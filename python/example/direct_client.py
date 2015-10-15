# -*- coding: utf-8 -*-

__author__ = 'ruoshui.jin'
__date__ = '2015/5/27'

import time
import logging

from tutorial import TutorialService
from bfd.harpc import client
from bfd.harpc.common import config

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename='./logs/directclient.log',
                    filemode='w')

if __name__ == '__main__':
    # read config file
    # conf = config.Config("./etc/direct_client.conf")

    # setting config direct connect
    conf = config.Config()
    conf.set("client", "use_zk", "False") # 不是用zk
    conf.set("client", "direct_address", "172.18.1.101:19999") # ip：port 对应server的ip 和 端口

    manager = client.Client(TutorialService.Client, conf)
    proxy_client = manager.create_proxy()
    for i in range(0, 4):
        print proxy_client.echo("hello world!")
        time.sleep(0.1)
    manager.close()
