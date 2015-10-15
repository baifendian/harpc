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

import socket
import logging
import signal
import threading
import functools
import time
import inspect
import json
import gevent

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from kazoo.client import KazooState
from kazoo.security import make_acl
from kazoo.security import make_digest_acl
from kazoo.exceptions import AuthFailedException
from kazoo.exceptions import NoAuthException
from bfd.harpc.common import utils

from bfd.harpc.zkclient import HARpcZKClientManager
from bfd.harpc.collector import StatisticsCollector
from bfd.harpc import settings

from bfd.harpc.common import monkey
monkey.patch_thrift()


class ServerBase(object):
    """
        harpc python serverbase
    """
    def __init__(self, processor, handler, config):
        self._logger = logging.getLogger(__name__)
        self._section_name = utils.get_module(__name__)
        self._server = None
        self._service_name = config.get(self._section_name, "service", required=True)
        self._port = config.getint(self._section_name, "port", required=True)
        self._zk_connect_str = config.get(self._section_name, "zk_connect_str", required=True)
        self._auth_user = config.get(self._section_name, "auth_user", required=True)
        self._auth_password = config.get(self._section_name, "auth_password", required=True)
        self._monitor = config.getboolean(self._section_name, "monitor", default=settings.SERVICE_MONITOR)
        self._c_module_serialize = config.getboolean(self._section_name, "c_module_serialize",
                                                     default=settings.USE_C_MODULE_SERIALIZE)
        self._server_name = config.get(self._section_name, "name")
        self._owner = config.get(self._section_name, "owner")

        hosts = "%s/%s" % (self._zk_connect_str, settings.DEFAULT_ZK_NAMESPACE_ROOT)

        self._server_path = "%s/%s" % (self._service_name, settings.DEFAULT_ZK_NAMESPACE_SERVERS)
        self._statistic_path = "%s/%s/%s" % (self._service_name, settings.DEFAULT_ZK_NAMESPACE_STATISTICS,
                                             settings.DEFAULT_ZK_NAMESPACE_SERVERS)
        # create zk acl
        self._acls = []
        self._acls.append(make_digest_acl(self._auth_user, self._auth_password, all=True))
        self._acls.append(make_acl("world", "anyone", read=True))
        # create zk_client
        self._zkclient = HARpcZKClientManager.make(hosts, config, "server")
        self._zkclient.add_auth("digest", "%s:%s" % (self._auth_user, self._auth_password))

        # create zkpath
        if not self._zkclient.exists(self._service_name):
            self._zkclient.create(self._service_name, makepath=True)
        if not self._zkclient.exists(self._server_path):
            self._zkclient.create(self._server_path, acl=self._acls)
        if not self._zkclient.exists(self._statistic_path):
            self._zkclient.create(self._statistic_path, makepath=True)

        self.transport = TSocket.TServerSocket(port=self._port)
        self.tfactory = TTransport.TBufferedTransportFactory()

        if self._monitor:
            self._collector = StatisticsCollector(self._zkclient, config, is_server=True)
            self._processor = self._statistics_wrapper(processor)(handler)
        else:
            self._processor = processor(handler)

        if self._c_module_serialize:
            self.pfactory = TBinaryProtocol.TBinaryProtocolAcceleratedFactory()
        else:
            self.pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    def start(self):
        """start server"""
        try:
            def state_listener(state):
                self._logger.debug("state listener state:%s", state)
                if state == KazooState.CONNECTED:
                    self._logger.debug("zk listener connected")
                    self._zkclient.handler.spawn(self._register_server)

            self._zkclient.add_listener(state_listener)
        except Exception, e:
            self._logger.exception(e)
            raise e
        self._start()

    def _start(self):
        pass

    def stop(self):
        self._server.stop()

    def _register_server(self):
        """register server"""
        local_ip = socket.gethostbyname(socket.gethostname())
        server_node_path = "%s/%s/%s:%s" % (
            self._service_name, settings.DEFAULT_ZK_NAMESPACE_SERVERS, local_ip,
            self._port)
        while True:
            try:
                if not self._zkclient.exists(server_node_path):
                    path = self._zkclient.create(server_node_path, acl=self._acls, ephemeral=True)
                    if self._monitor:
                        interval = self._collector.interval
                    else:
                        interval = None
                    server_info = {"name": self._server_name, "owner": self._owner,
                               "port": self._port, "ip": local_ip, "service": self._service_name,
                               "monitor": self._monitor, "interval": interval,
                               "maxWorkerThreads": self._process_num}
                    self._zkclient.set(path, json.dumps(server_info))
                    self._node_name = path[path.rindex("/") + 1:]
                break
            except (NoAuthException,AuthFailedException) as e:
                raise e
            except Exception as e:
                self._logger.exception(e)
                gevent.sleep(1)

    def _post_start_callback(self):
        """run after server start"""
        self._register_server()
        if self._monitor:
            self._collector.set_path("%s/%s" % (self._statistic_path, self._node_name))
            self._collector.start()

    def _statistics_wrapper(self, processor):
        """wrapper process function for statistics"""
        def add_statistic(func):
            @functools.wraps(func)
            def wrapper(*args, **kwargs):
                start = time.time()
                flag = "SUCCESS"
                try:
                    func(*args, **kwargs)
                except Exception, e:
                    flag = "FAILED"
                    raise e
                finally:
                    end = time.time()
                    self._collector.collect(flag, end - start)
            return wrapper
        for name, func in inspect.getmembers(processor, predicate=inspect.ismethod):
            if name.startswith("process_"):
                setattr(processor, name, add_statistic(func))
        return processor


class ProcessPoolThriftServer(ServerBase):
    """ thrift TProcessPoolThriftServer"""

    def __init__(self, processor, handler, config):
        try:
            super(ProcessPoolThriftServer, self).__init__(processor, handler, config)

            self._process_num = config.getint(self._section_name, "process_num", default=settings.DEFAULT_PROCESS_NUM)
            from thrift.server import TProcessPoolServer

            self._server = TProcessPoolServer.TProcessPoolServer(self._processor, self.transport,
                                                                 self.tfactory, self.pfactory)
            self._server.setNumWorkers(self._process_num)

            def clean_shutdown(signum, frame):
                for worker in self._server.workers:
                    worker.terminate()
                try:
                    threading.Thread(target=self.stop).start()
                except:
                    pass

            def add_clean_shutdown(func):
                @functools.wraps(func)
                def wrapper(*args, **kwargs):
                    clean_shutdown(*args, **kwargs)
                    return func(*args, **kwargs)

                return wrapper

            sigint_handler = signal.getsignal(signal.SIGINT)
            if callable(sigint_handler):
                signal.signal(signal.SIGINT, add_clean_shutdown(sigint_handler))
            else:
                signal.signal(signal.SIGINT, clean_shutdown)
        except Exception, e:
            self._logger.exception(e)
            raise e

    def _start(self):
        self._post_start_callback()
        self._server.serve()

    def set_post_fork_callback(self, callback):
        self._server.setPostForkCallback(callback)

    def set_num_process(self, num_workers):
        self._server.setNumWorkers(num_workers)


class GeventProcessPoolThriftServer(ServerBase):
    """ thrift GeventProcessPoolThriftServer"""

    def __init__(self, processor, handler, config):
        try:
            super(GeventProcessPoolThriftServer, self).__init__(processor, handler, config)
            self._process_num = config.getint(self._section_name, "process_num", default=settings.DEFAULT_PROCESS_NUM)
            self._coroutines_num = config.getint(self._section_name, "coroutines_num", default=settings.DEFAULT_COROUTINES_NUM)
            from thrift_server import gevent_processpool_server

            self._server = gevent_processpool_server.GeventProcessPoolServer(self._processor, self.transport,
                                                                             self.tfactory, self.pfactory)
            self._server.set_num_process(self._process_num)
            self._server.set_num_coroutines(self._coroutines_num)

            def clean_shutdown(signum, frame):
                for worker in self._server.workers:
                    worker.terminate()
                try:
                    threading.Thread(target=self.stop).start()
                except:
                    pass

            sigint_handler = signal.getsignal(signal.SIGINT)
            if callable(sigint_handler):
                def add_clean_shutdown(method):
                    def wrapper(*args, **kwargs):
                        clean_shutdown(*args, **kwargs)
                        return method(*args, **kwargs)

                    return wrapper

                signal.signal(signal.SIGINT, add_clean_shutdown(sigint_handler))
            else:
                signal.signal(signal.SIGINT, clean_shutdown)
        except Exception, e:
            self._logger.exception(e)
            raise e

    def _start(self):
        self._server.set_post_start_callback(self._post_start_callback)
        self._server.serve()

    def set_post_fork_callback(self, callback):
        self._server.setPostForkCallback(callback)

    def set_num_process(self, num_process):
        self._server.set_num_process(num_process)

    def set_num_coroutines(self, num_coroutines):
        self._server.set_num_coroutines(num_coroutines)
