# -*- coding: utf-8 -*-
import sys
import logging
import gevent
import time
import traceback
from multiprocessing import Process
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

Coroutines = 5
req_num = 50
workers = 4
data = []
for i in range(0,1024):
    data.append(chr(i%64 + 32))

test_msg= ''.join(data)

def test():
    conf = config.Config()
    conf.set("client", "service", "python_test$EchoService")
    conf.set("client", "zk_connect_str", "172.18.1.22:2181")
    # 每个进程创建一个 client，多个协程公用一个client
    manager = client.Client(TutorialService.Client, conf)
    proxy_client = manager.create_proxy()

    def test_echo(msg):
        for i in xrange(req_num):
            try:
                proxy_client.echo(msg)
            except Exception, e:
                print "request error:%s" % e
    jobs = []
    for i in range(Coroutines):
        jobs.append(gevent.spawn(test_echo,test_msg))
    gevent.joinall(jobs)
if __name__ == "__main__":
    p_list = []
    start =  time.time()
    for i in range(workers):
        p = Process(target=test)
        p_list.append(p)
        p.start()
    for p in p_list:
        p.join()
    end = time.time()
    req_time = end-start
    total = req_num*workers*Coroutines
    print "total     : %s" % total
    print "total time: %s" % req_time
    print "tps       : %s" % (total/req_time)
