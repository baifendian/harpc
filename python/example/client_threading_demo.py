# -*- coding: utf-8 -*-
import threading
import logging
import time

from tutorial import TutorialService
from bfd.harpc import client
from bfd.harpc.common import config


threads = 15
req_num = 10
data = []
error = 0
for i in range(0,10240):
    data.append(chr(i%64 + 32))
test_msg= ''.join(data)

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename='./logs/clientdemo.log',
                    filemode='w')



def process(proxy_client, process_num):
    global error
    for i in range(0,req_num):
        try:
            proxy_client.echo(test_msg)
        except Exception as e:
            error = error + 1
            print "request error %s" %e
    print("process_num:%s end" % process_num)

if __name__ == '__main__':
    # read config file
    #conf = config.Config("./etc/demo_client.conf")
    # setting config  use zk
    conf = config.Config()
    conf.set("client", "service", "python_test$EchoService")
    conf.set("client", "zk_connect_str", "172.18.1.22:2181")
    manager = client.Client(TutorialService.Client, conf)
    proxy_client = manager.create_proxy()
    jobs = []
    # 创建多线程，多线程公用一个proxy_client
    start =  time.time()
    for i in range(0, threads):
         td = threading.Thread(target=process, args=(proxy_client, i))
         td.start()
         jobs.append(td)
    for job in jobs:
        job.join()
    end = time.time()
    req_time = end-start
    total = req_num*threads
    print "total     : %s" % total
    print "total time: %s" % req_time
    print "error num : %s" % error
    print "tps       : %s" % (total/req_time)

    manager.close()
