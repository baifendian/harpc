# -*- coding: utf-8 -*-

# Copyright (C) 2015 Baifendian Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import logging
from multiprocessing import Process, Value, Condition, Semaphore
from thrift.server.TServer import TServer
from thrift.transport.TTransport import TTransportException
import gevent
import gevent.pool
import gevent.monkey

# patch os.fork
gevent.monkey.patch_os()
gevent.monkey.patch_socket()
gevent.monkey.patch_time()
gevent.monkey.patch_ssl()

logger = logging.getLogger(__name__)


class GeventProcessPoolServer(TServer):
    """Server with a fixed size pool of worker subprocesses which has a gevent pool to service requests"""

    def __init__(self, *args):
        TServer.__init__(self, *args)
        self.workers = []
        self.isRunning = Value('b', False)
        self.postForkCallback = None
        self._post_start_callback = None

    def setPostForkCallback(self, callback):
        if not callable(callback):
            raise TypeError("This is not a callback!")
        self.postForkCallback = callback

    def workerProcess(self, semaphore):
        """Loop getting clients from the shared queue and process them"""
        if self.postForkCallback:
            self.postForkCallback()

        semaphore.release()

        pool = gevent.pool.Pool(self.num_coroutines)

        while self.isRunning.value:
            try:
                client = self.serverTransport.accept()
                if not client:
                    continue
                pool.spawn(self.serveClient, client)
            except (KeyboardInterrupt, SystemExit):
                return 0
            except Exception, x:
                logger.exception(x)

    def serveClient(self, client):
        """Process input/output from a client for as long as possible"""
        itrans = self.inputTransportFactory.getTransport(client)
        otrans = self.outputTransportFactory.getTransport(client)
        iprot = self.inputProtocolFactory.getProtocol(itrans)
        oprot = self.outputProtocolFactory.getProtocol(otrans)

        try:
            while True:
                self.processor.process(iprot, oprot)
        except TTransportException, tx:
            pass
        except Exception, x:
            logger.exception(x)

        itrans.close()
        otrans.close()

    def serve(self):
        """Start workers and put into queue"""
        # this is a shared state that can tell the workers to exit when False
        self.isRunning.value = True

        # first bind and listen to the port
        self.serverTransport.listen()

        # fork the children
        semaphore = Semaphore(0)
        for i in range(self.numWorkers):
            try:
                w = Process(target=self.workerProcess, args=(semaphore,))
                w.daemon = True
                w.start()
                self.workers.append(w)
            except Exception, x:
                logger.exception(x)

        # wait until all workers init finish
        for i in range(self.numWorkers):
            semaphore.acquire()
        self._post_start_callback()

        # wait until the condition is set by stop()
        while True:
            try:
                gevent.sleep(1)
                if not self.isRunning.value:
                    break
            except (SystemExit, KeyboardInterrupt):
                break
            except Exception, x:
                logger.exception(x)

        self.isRunning.value = False

    def stop(self):
        self.isRunning.value = False

    def set_num_process(self, num):
        self.numWorkers = num

    def set_num_coroutines(self, num):
        self.num_coroutines = num

    def set_post_start_callback(self, callback):
        if not callable(callback):
            raise TypeError("This is not a callback!")
        self._post_start_callback = callback
