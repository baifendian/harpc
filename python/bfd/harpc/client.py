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

import inspect
import re
import socket
import time
import logging
import json

from thrift.transport import TTransport
from kazoo.client import KazooState
from kazoo.recipe.watchers import ChildrenWatch
from bfd.harpc import settings
from bfd.harpc.zkclient import HARpcZKClientManager
from bfd.harpc.collector import StatisticsCollector
from bfd.harpc.exceptions import RpcException
from bfd.harpc.loadbalancer import LoadBalancer
from bfd.harpc.dynamic_host_set import DynamicHostSet
from bfd.harpc.connection_pool import ConnectionPool
from bfd.harpc.common import utils


class Client(object):
    """harpc python client"""

    def __init__(self, client_cls, config):
        self._logger = logging.getLogger(__name__)
        # get file name filter .py
        self._section_name = utils.get_module(__name__)
        self.config = config
        try:
            self._client_cls = client_cls

            self._use_zk = config.getboolean(self._section_name, "use_zk", default=settings.DEFAULT_USE_ZK)
            self._retries = config.getint(self._section_name, "retry", default=settings.DEFAULT_REQUEST_RETRY)
            self._balance_path = config.get(self._section_name, "balance", default=settings.LOAD_BALANCE_PATH)

            balance_strategy = utils.import_class(self._balance_path)

            self._client_pool_map = {}
            self._dynamic_host_set = DynamicHostSet(config)

            if self._use_zk:
                self._service_name = config.get(self._section_name, "service", required=True)
                self._monitor = config.getboolean(self._section_name, "monitor", default=settings.SERVICE_MONITOR)
                self._zk_connect_str = config.get(self._section_name, "zk_connect_str", required=True)
                self._client_name = config.get(self._section_name, "name")
                self._owner = config.get(self._section_name, "owner")

                hosts = "%s/%s" % (self._zk_connect_str, settings.DEFAULT_ZK_NAMESPACE_ROOT)
                self._server_path = "%s/%s" % (self._service_name, settings.DEFAULT_ZK_NAMESPACE_SERVERS)
                self._client_path = "%s/%s" % (self._service_name, settings.DEFAULT_ZK_NAMESPACE_CLIENTS)
                self._statistic_path = "%s/%s/%s" % (
                    self._service_name, settings.DEFAULT_ZK_NAMESPACE_STATISTICS, settings.DEFAULT_ZK_NAMESPACE_CLIENTS)
                # zk client node name, because of use zk sequence, so need save
                self._client_node_name = None
                self._zkclient = HARpcZKClientManager.make(hosts, config, "client")

                # register client
                if not self._zkclient.exists(self._client_path):
                    self._zkclient.create(self._client_path, makepath=True)
                if not self._zkclient.exists(self._statistic_path):
                    self._zkclient.create(self._statistic_path, makepath=True)

                self._collector = StatisticsCollector(self._zkclient, config) if self._monitor else None
                self._register_client()

                def state_listener(state):
                    self._logger.debug("state listener state:%s" % state)
                    if state == KazooState.CONNECTED:
                        self._zkclient.handler.spawn(self._re_register_client)

                self._zkclient.add_listener(state_listener)

                self._loadbalancer = LoadBalancer(balance_strategy(),
                                                  self._dynamic_host_set, config, collector=self._collector)

                self._dynamic_host_set.reset_with_list(self._zkclient.get_children(self._server_path))
                # add children listener
                ChildrenWatch(self._zkclient, self._server_path, func=self._watch_server_node)
                if self._collector:
                    self._collector.set_path("%s/%s" % (self._statistic_path, self._client_node_name))
                    self._collector.start()
            else:
                connect_str = config.get(self._section_name, "direct_address", required=True)
                if re.match(
                        r"^(\d{1,3}\.){3}\d{1,3}:\d{1,5}(;(\d{1,3}\.){3}\d{1,3}:\d{1,5})*$",
                        connect_str):
                    server_nodes = connect_str.split(";")
                    self._loadbalancer = LoadBalancer(balance_strategy(), self._dynamic_host_set, config)
                    self._dynamic_host_set.reset_with_list(server_nodes)
                    for server_node in server_nodes:
                        self._client_pool_map[server_node] = ConnectionPool(server_node, self._client_cls, self.config)
                else:
                    raise AttributeError("server address is invalid!")
        except Exception, e:
            self._logger.exception(e)
            raise e

    def _register_client(self):
        """zk client register"""
        local_ip = socket.gethostbyname(socket.gethostname())
        client_node_path = "%s/%s/%s:0:i_" % (
            self._service_name, settings.DEFAULT_ZK_NAMESPACE_CLIENTS, local_ip)
        while True:
            try:
                if self._collector:
                    interval = self._collector.interval
                else:
                    interval = None
                if not self._client_node_name:
                    path = self._zkclient.create(client_node_path, ephemeral=True, sequence=True)
                    self.client_info = {"name": self._client_name, "owner": self._owner,
                               "retry": self._retries, "ip": local_ip, "service": self._service_name,
                               "monitor": self._monitor, "interval": interval,
                               "loadbalance": self._balance_path}

                    self._zkclient.set(path, json.dumps(self.client_info))
                    self._client_node_name = path[path.rindex("/") + 1:]
                break
            except Exception, e:
                self._logger.exception(e)
                time.sleep(1)

    def _re_register_client(self):
        """zk client register"""
        client_node_path = "%s/%s/%s" % (
            self._service_name, settings.DEFAULT_ZK_NAMESPACE_CLIENTS, self._client_node_name)
        while True:
            try:
                if not self._zkclient.exists(client_node_path):
                    self._zkclient.create(client_node_path, ephemeral=True, sequence=False)
                    self._zkclient.set(client_node_path, json.dumps(self.client_info))
                break
            except Exception, e:
                self._logger.exception(e)
                time.sleep(1)

    def _watch_server_node(self, server_nodes):
        """zk server path linster"""
        for server_node in server_nodes:
            if server_node not in self._client_pool_map.keys():
                self._logger.debug("watch server node:%s connection pool create" % server_node)
                self._client_pool_map[server_node] = ConnectionPool(server_node, self._client_cls, self.config)
        if len(server_nodes) > 0:
            self._dynamic_host_set.reset_with_list(server_nodes)
        else:
            self._logger.warn("zk register server num is 0")

    def close(self):
        """client close"""
        for client_pool in self._client_pool_map.values():
            client_pool.close()

    def create_proxy(self):
        # create proxy and inject all methods defined in the proxy class
        iface = self._client_cls.__bases__[0]
        proxy = iface()
        for m in inspect.getmembers(iface, predicate=inspect.ismethod):
            setattr(proxy, m[0], self._create_method_proxy(m[0]))
        return proxy

    def _create_method_proxy(self, method_name):
        """create method proxy"""

        def _method_proxy(*args):
            return self._method_call(method_name, *args)

        return _method_proxy

    def _method_call(self, method, *args):
        """rpc method call"""
        for retry in range(self._retries):
            start = time.time()
            try:
                server_node = self._loadbalancer.get_backend()
            except Exception:
                raise RpcException("No server found!!")
            try:
                client_pool = self._client_pool_map.get(server_node)
                conn = client_pool.get_connection()
            except Exception, e:
                self._logger.warn("Get connection failed! msg:%s" % e)
                if retry == self._retries - 1:
                    self._logger.exception(e)
                    raise RpcException("Get connection failed!")
                continue
            try:
                # rpc request
                result = getattr(conn, method)(*args)
                #result = ""
                self._loadbalancer.request_result(server_node, "SUCCESS", (time.time() - start))
                client_pool.return_connection(conn)
                # self._logger.debug("RPC Method Call:%s Server:%s" % (method, server_node))
                return result
            except socket.timeout, e:
                # timeout
                self._loadbalancer.request_result(server_node, "TIMEOUT", (time.time() - start))
                client_pool.release_connection(conn)
                self._logger.warn("RPC request timeout! Server:%s" % server_node)
                if retry == self._retries - 1:
                    self._logger.exception(e)
                    raise RpcException("Connection request timeout!")
            except Exception, e:
                # data exceptions, return connection     broken connection, release it
                self._loadbalancer.request_result(server_node, "FAILED", (time.time() - start))
                if isinstance(e, socket.error) or isinstance(e, TTransport.TTransportException):
                    self._logger.warn("scoket exception release all connection server:%s, mgs:%s" % (server_node, e))
                    client_pool.release_connection(conn)
                    client_pool.release_all_connection()
                else:
                    client_pool.return_connection(conn)
                self._logger.warn("RPC request error! msg:%s " % e)
                if retry == self._retries - 1:
                    self._logger.exception(e)
                    raise RpcException("Connection request failed!")
