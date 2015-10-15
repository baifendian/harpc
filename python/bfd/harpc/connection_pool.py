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

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from bfd.harpc.common import utils
from bfd.harpc import settings
ASYNC_TAG = False


class ConnectionPool(object):
    """dynamic service connection pool"""

    def __init__(self, server_node, iface_cls, config):

        self._section_name = utils.get_module(__name__)
        self._logger = logging.getLogger(__name__)
        self._host = server_node.split(":")[0]
        self._port = int(server_node.split(":")[1])
        self._iface_cls = iface_cls

        self._get_conn_timeout = config.getint(self._section_name, "pool_timeout",
                                               default=settings.DEFAULT_POOL_TIMEOUT)
        self._socket_timeout = config.getint(self._section_name, "request_timeout",
                                             default=settings.DEFAULT_REQUEST_TIMEOUT) * 1000
        self._size = config.getint(self._section_name, "pool_size", default=settings.DEFAULT_POOL_SIZE)

        self._c_module_serialize = config.getboolean(self._section_name, "c_module_serialize",
                                                     default=settings.USE_C_MODULE_SERIALIZE)

        self._closed = False
        if ASYNC_TAG:
            from gevent.lock import BoundedSemaphore
            from gevent import queue as Queue
            self._semaphore = BoundedSemaphore(self._size)
            self._connection_queue = Queue.LifoQueue(self._size)
            self._QueueEmpty = Queue.Empty
        else:
            from threading import BoundedSemaphore
            import Queue
            self._semaphore = BoundedSemaphore(self._size)
            self._connection_queue = Queue.LifoQueue(self._size)
            self._QueueEmpty = Queue.Empty

    def close(self):
        self._closed = True
        while not self._connection_queue.empty():
            try:
                conn = self._connection_queue.get(block=False)
                try:
                    self._close_connection(conn)
                except:
                    pass
            except self._QueueEmpty:
                pass

    def _create_connection(self):
        self._logger.debug("create a new connection ip:%s port:%s" %(self._host, self._port))
        socket_ = TSocket.TSocket(self._host, self._port)
        if self._socket_timeout > 0:
            socket_.setTimeout(self._socket_timeout)
        transport = TTransport.TBufferedTransport(socket_)
        if self._c_module_serialize:
            protocol = TBinaryProtocol.TBinaryProtocolAccelerated(transport)
        else:
            protocol = TBinaryProtocol.TBinaryProtocol(transport)
        connection = self._iface_cls(protocol)
        transport.open()
        return connection

    def _close_connection(self, conn):
        try:
            conn._iprot.trans.close()
        except:
            pass
        try:
            conn._oprot.trans.close()
        except:
            pass

    def get_connection(self):
        """ get a connection from the pool. This blocks until one is available."""
        self._semaphore.acquire()
        if self._closed:
            raise RuntimeError('connection pool closed')
        try:
            return self._connection_queue.get(block=False)
        except self._QueueEmpty:

            try:
                return self._create_connection()
            except Exception as e:
                self._semaphore.release()
                raise e

    def return_connection(self, conn):
        """ return a connection to the pool."""
        if self._closed:
            self._close_connection(conn)
            return
        self._connection_queue.put(conn)
        self._semaphore.release()

    def release_connection(self, conn):
        """ call when the connect is no usable anymore"""
        try:

            self._close_connection(conn)
        except:
            pass
        if not self._closed:
            self._semaphore.release()

    def release_all_connection(self):
        """ call when the all connect in pool is no usable anymore"""
        while not self._connection_queue.empty():
            try:
                conn = self._connection_queue.get(block=False)
                try:
                    self._close_connection(conn)
                except:
                    pass
            except self._QueueEmpty:
                pass
