# -*- coding: utf-8 -*-
import sys
import logging
import gevent
import time

from tutorial import TutorialService
from bfd.harpc import client
from bfd.harpc.common import config
from bfd.harpc.common import monkey
# 协程调用的时候要进行打入package，替换掉线程的一些库
monkey.patch_all()

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename='./logs/clientdemo.log',
                    filemode='w')

Coroutines = 15
req_num = 10
data = []
error = 0
for i in range(0,10240):
    data.append(chr(i%64 + 32))
test_msg= ''.join(data)

def test():
    # 创建协程，多协程公用一个proxy_client
    conf = config.Config()
    conf.set("client", "service", "python_test$EchoService")
    conf.set("client", "zk_connect_str", "172.18.1.22:2181")
    manager = client.Client(TutorialService.Client, conf)
    proxy_client = manager.create_proxy()

    def test_echo(msg):
        global error
        for i in xrange(req_num):
            try:
                proxy_client.echo(msg)
            except Exception, e:
                error += 1
    jobs = []
    for i in range(Coroutines):
        jobs.append(gevent.spawn(test_echo,test_msg))
    gevent.joinall(jobs)
if __name__ == "__main__":
    start =  time.time()
    test()
    end = time.time()
    req_time = end-start
    total = req_num*Coroutines
    print "total     : %s" % total
    print "total time: %s" % req_time
    print "error num : %s" % error
    print "tps       : %s" % (total/req_time)

