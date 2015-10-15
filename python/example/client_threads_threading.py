# -*- coding: utf-8 -*-
import threading
import logging
import time
import json

from tutorial import TutorialService
from bfd.harpc import client
from bfd.harpc.common import config
from multiprocessing import Process


logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename='./logs/clientdemo.log',
                    filemode='w')

req_num = 10
workers = 4
threads = 4
data = []
for i in range(0,1024):
    data.append(chr(i%64 + 32))

data = ''.join(data)

def process(process_num):
    conf = config.Config()
    conf.set("client", "service", "python_test$EchoService")
    conf.set("client", "zk_connect_str", "172.18.1.22:2181")
    # 每个进程创建一个 client，多个线程公用一个client
    manager = client.Client(TutorialService.Client, conf)
    proxy_client = manager.create_proxy()
    jobs = []
    def test(num):
        for i in range(0,req_num):
            try:
                proxy_client.echo(data)
            except Exception as e:
                print "request error: %s" %e
    thread_jobs = []
    for j in range(0, threads):
        ttd = threading.Thread(target=test, args=(j,))
        ttd.start()
        thread_jobs.append(ttd)
    for thread_job in thread_jobs:
        thread_job.join()


if __name__ == '__main__':
    jobs = []
    start = time.time()
    for i in range(0, workers):
         td = Process(target=process, args=(i,))
         td.start()
         jobs.append(td)

    for job in jobs:
        job.join()
    end = time.time()

    req_time = end-start
    total = req_num*workers*threads
    print "total     : %s" % total
    print "total time: %s" % req_time
    print "tps       : %s" % (total/req_time)
    print "all job stop"
