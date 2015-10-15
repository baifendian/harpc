# -*- coding: utf-8 -*-
"""
Copyright (C) 2015 Baifendian Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import json
import logging
import threading
from django.conf import settings
from kazoo.client import KazooClient
from kazoo.security import make_digest_acl, make_acl
import time
from manage.lib.harpcutils import getExceptionDesc, safe_list_get

__author__ = 'caojingwei'

logger = logging.getLogger('manage.libs')


class ZKWrap(object):
    def __init__(self):
        self.zk_hosts = settings.ZK_HOSTS
        self.zk_root = settings.ZK_ROOT
        self.zk_timeout = settings.ZK_TIMEOUT
        self.zk_servers = settings.ZK_SERVERS
        self.zk_clients = settings.ZK_CLIENTS
        self.zk_configs = settings.ZK_CONFIGS
        self.zk_statistics = settings.ZK_STATISTICS
        self.zk_username = settings.ZK_USERNAME
        self.zk_password = settings.ZK_PASSWORD

        self.zk_flush = time.time()

        self.lock = threading.Lock()
        self.resource = {}

        self.zk_harpc_acl = make_digest_acl(username=self.zk_username, password=self.zk_password, all=True)
        self.zk_anyone_acl = make_acl(scheme='world', credential='anyone', read=True)


    def children_callback(self, children):
        print '****', children

    def get_children(self, path):
        return self.zk.get_children(path)

    def get(self, path):
        return self.zk.get(path)

    def get_acls(self, path):
        return self.zk.get_acls(path)

    def load_zk(self):
        self.init_connect()

    def get_resource(self):
        return self.resource

    def init_connect(self):
        try:
            self.zk = KazooClient(hosts=self.zk_hosts, timeout=self.zk_timeout)
            self.zk.start(timeout=self.zk_timeout)
            self.zk.add_auth(scheme='digest', credential=self.zk_username + ':' + self.zk_password)
            logger.info("connect to zk servers")
        except Exception, e:
            logger.error('manage.ZKWrap.init_connect:' + getExceptionDesc(e))
            return False

    def load_zk_resource(self):
        logger.info('get service from zookeeper')
        # 如果系统节点数过多请注释该条不启用监听器实时更新节点信息，以避免ZK性能受到影响
        # service_list = self.zk.get_children(path=self.zk_root, watch=self.watch_services)
        service_list = self.zk.get_children(path=self.zk_root)
        services = {}
        for service in service_list:

            acl = False
            try:
                result = self.get_acls("%s/%s/%s" % (self.zk_root, service, self.zk_servers))
                acls = safe_list_get(result, 0, [])
                if self.zk_harpc_acl in acls:
                    acl = True
            except Exception, e:
                acl = False

            servers = {}
            # 如果系统节点数过多请注释该条不启用监听器实时更新节点信息，以避免ZK性能受到影响
            # if self.zk.exists(path="%s/%s/%s" % (self.zk_root,service,self.zk_servers),watch=self.watch_servers_exists):
            if self.zk.exists(path="%s/%s/%s" % (self.zk_root, service, self.zk_servers)):
                # 如果系统节点数过多请注释该条不启用监听器实时更新节点信息，以避免ZK性能受到影响
                # server_list= self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, service, self.zk_servers),watch=self.watch_servers)
                server_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, service, self.zk_servers))
                for server in server_list:
                    server_data_dict = {}
                    server_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, service, self.zk_servers, server))
                    try:
                        server_data_str = safe_list_get(server_data, 0, "{}")
                        server_data_dict = json.loads(server_data_str)
                    except Exception, e:
                        logger.error(
                            'manage.ZKWrap.load_zk_resource getdata:' + service + "/" + server + "/" + getExceptionDesc(
                                e))
                        pass
                    # if len(server_data_str) > 2:
                    #     server_data_dict = eval(server_data_str)
                    server_node_stat = safe_list_get(server_data, 1, None)
                    if server_node_stat:
                        server_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                                  time.localtime(int(server_node_stat.ctime / 1000)))
                    server_acl = False
                    result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, service, self.zk_servers, server))
                    acls = safe_list_get(result, 0, [])
                    if self.zk_harpc_acl in acls:
                        server_acl = True

                    servers[server] = {"data": server_data_dict, "acl": server_acl}

            clients = {}
            # 如果系统节点数过多请注释该条不启用监听器实时更新节点信息，以避免ZK性能受到影响
            # if self.zk.exists(path="%s/%s/%s" % (self.zk_root,service,self.zk_clients),watch=self.watch_clients_exists):
            if self.zk.exists(path="%s/%s/%s" % (self.zk_root, service, self.zk_clients)):
                # 如果系统节点数过多请注释该条不启用监听器实时更新节点信息，以避免ZK性能受到影响
                # client_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, service, self.zk_clients),watch=self.watch_clients)
                client_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, service, self.zk_clients))
                for client in client_list:
                    client_data_dict = {}
                    client_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, service, self.zk_clients, client))
                    try:
                        client_data_str = safe_list_get(client_data, 0, "{}")
                        client_data_dict = json.loads(client_data_str)
                    except Exception, e:
                        logger.error(
                            'manage.ZKWrap.load_zk_resource getdata:' + service + "/" + client + ":" + getExceptionDesc(
                                e))
                        pass
                    client_node_stat = safe_list_get(client_data, 1, None)
                    if client_node_stat:
                        client_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                                  time.localtime(int(client_node_stat.ctime / 1000)))
                    client_acl = False
                    result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, service, self.zk_clients, client))
                    acls = safe_list_get(result, 0, [])
                    if self.zk_harpc_acl in acls:
                        client_acl = True

                    clients[client] = {"data": client_data_dict, "acl": client_acl}
            services[service] = {self.zk_servers: servers, self.zk_clients: clients, "acl": acl}

        self.lock.acquire()
        self.resource = services
        self.lock.release()

        logger.info('finish getting services from zookeeper')

    def watch_services(self, event):
        logger.info("services is change:" + str(event))
        new_service_list = self.zk.get_children(path="/%s" % (self.zk_root), watch=self.watch_services)
        new_service = set(new_service_list)
        old_service = set(self.resource.keys())
        add_services = new_service - old_service
        del_services = old_service - new_service

        for add_service in add_services:
            acl = False
            try:
                result = self.get_acls("%s/%s/%s" % (self.zk_root, add_service, self.zk_servers))
                acls = safe_list_get(result, 0, [])
                if self.zk_harpc_acl in acls:
                    acl = True
            except Exception, e:
                acl = False

            servers = {}
            if self.zk.exists(path="%s/%s/%s" % (self.zk_root, add_service, self.zk_servers),
                              watch=self.watch_servers_exists):
                server_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, add_service, self.zk_servers),
                                                   watch=self.watch_servers)
                for server in server_list:
                    server_data_dict = {}
                    server_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, add_service, self.zk_servers, server))
                    try:
                        server_data_str = safe_list_get(server_data, 0, "{}")
                        server_data_dict = json.loads(server_data_str)
                    except Exception, e:
                        logger.error(
                            'manage.ZKWrap.watch_services getdata:' + add_service + "/" + server + ":" + getExceptionDesc(
                                e))
                        pass

                    server_node_stat = safe_list_get(server_data, 1, None)
                    if server_node_stat:
                        server_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                                  time.localtime(int(server_node_stat.ctime / 1000)))

                    server_acl = False
                    result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, add_service, self.zk_servers, server))
                    acls = safe_list_get(result, 0, [])
                    if self.zk_harpc_acl in acls:
                        server_acl = True

                    servers[server] = {"data": server_data_dict, "acl": server_acl}

            clients = {}
            if self.zk.exists(path="%s/%s/%s" % (self.zk_root, add_service, self.zk_clients),
                              watch=self.watch_clients_exists):
                client_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, add_service, self.zk_clients),
                                                   watch=self.watch_clients)
                for client in client_list:
                    client_data_dict = {}
                    client_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, add_service, self.zk_clients, client))
                    try:
                        client_data_str = safe_list_get(client_data, 0, "{}")
                        client_data_dict = json.loads(client_data_str)
                    except Exception, e:
                        logger.error(
                            'manage.ZKWrap.watch_services getdata:' + add_service + "/" + client + ":" + getExceptionDesc(
                                e))
                        pass

                    client_node_stat = safe_list_get(client_data, 1, None)
                    if client_node_stat:
                        client_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                                  time.localtime(int(client_node_stat.ctime / 1000)))

                    client_acl = False
                    result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, add_service, self.zk_clients, client))
                    acls = safe_list_get(result, 0, [])
                    if self.zk_harpc_acl in acls:
                        client_acl = True

                    clients[client] = {"data": client_data_dict, "acl": client_acl}

            self.lock.acquire()
            self.resource[add_service] = {self.zk_servers: servers, self.zk_clients: clients, "acl": acl}
            self.lock.release()

        for del_service in del_services:
            self.lock.acquire()
            del self.resource[del_service]
            self.lock.release()

    def watch_servers(self, event):
        logger.info("servers is change:" + str(event))
        type = event.type
        path = event.path
        service = path.split("/")[-2]
        new_server_list = self.zk.get_children(path=path, watch=self.watch_servers)
        if type == 'CHILD':
            new_servers = set(new_server_list)
            old_servers = set(self.resource[service][self.zk_servers].keys())

            add_servers = new_servers - old_servers
            del_servers = old_servers - new_servers

            for add_server in add_servers:
                server_data_dict = {}
                server_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, service, self.zk_servers, add_server))
                try:
                    server_data_str = safe_list_get(server_data, 0, "{}")
                    server_data_dict = json.loads(server_data_str)
                except Exception, e:
                    logger.error(
                        'manage.ZKWrap.watch_servers getdata:' + service + "/" + add_server + ":" + getExceptionDesc(e))
                    pass

                server_node_stat = safe_list_get(server_data, 1, None)
                if server_node_stat:
                    server_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                              time.localtime(int(server_node_stat.ctime / 1000)))

                server_acl = False
                result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, service, self.zk_servers, add_server))
                acls = safe_list_get(result, 0, [])
                if self.zk_harpc_acl in acls:
                    server_acl = True

                self.lock.acquire()
                self.resource[service][self.zk_servers][add_server] = {"data": server_data_dict, "acl": server_acl}
                self.lock.release()

            for del_server in del_servers:
                self.lock.acquire()
                del self.resource[service][self.zk_servers][del_server]
                self.lock.release()

    def watch_clients(self, event):
        logger.info("clients is change:" + str(event))
        type = event.type
        path = event.path
        service = path.split("/")[-2]
        new_client_list = self.zk.get_children(path=path, watch=self.watch_clients)
        if type == 'CHILD':
            new_clients = set(new_client_list)
            old_clients = set(self.resource[service][self.zk_clients].keys())

            add_clients = new_clients - old_clients
            del_clients = old_clients - new_clients

            for add_client in add_clients:
                client_data_dict = {}
                client_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, service, self.zk_clients, add_client))
                try:
                    client_data_str = safe_list_get(client_data, 0, "{}")
                    client_data_dict = json.loads(client_data_str)
                except Exception, e:
                    logger.error(
                        'manage.ZKWrap.watch_clients getdata:' + service + "/" + add_client + ":" + getExceptionDesc(e))
                    pass

                client_node_stat = safe_list_get(client_data, 1, None)
                if client_node_stat:
                    client_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                              time.localtime(int(client_node_stat.ctime / 1000)))

                client_acl = False
                result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, service, self.zk_clients, add_client))
                acls = safe_list_get(result, 0, [])
                if self.zk_harpc_acl in acls:
                    client_acl = True

                self.lock.acquire()
                self.resource[service][self.zk_clients][add_client] = {"data": client_data_dict, "acl": client_acl}
                self.lock.release()

            for del_client in del_clients:
                self.lock.acquire()
                del self.resource[service][self.zk_clients][del_client]
                self.lock.release()

    def watch_servers_exists(self, event):
        logger.info("servers_exists is change:" + str(event))
        type = event.type
        path = event.path
        service = path.split("/")[-2]
        if self.zk.exists(path=path, watch=self.watch_servers_exists):
            if type == 'CREATED':
                severs = {}
                server_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, service, self.zk_servers),
                                                   watch=self.watch_servers)
                for server in server_list:
                    server_data_dict = {}
                    server_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, service, self.zk_servers, server))
                    try:
                        server_data_str = safe_list_get(server_data, 0, "{}")
                        server_data_dict = json.loads(server_data_str)
                    except Exception, e:
                        logger.error(
                            'manage.ZKWrap.watch_servers_exists getdata:' + service + "/" + server + ":" + getExceptionDesc(
                                e))
                        pass

                    server_node_stat = safe_list_get(server_data, 1, None)
                    if server_node_stat:
                        server_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                                  time.localtime(int(server_node_stat.ctime / 1000)))

                    server_acl = False
                    result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, service, self.zk_servers, server))
                    acls = safe_list_get(result, 0, [])
                    if self.zk_harpc_acl in acls:
                        server_acl = True

                    severs[server] = {"data": server_data_dict, "acl": server_acl}

                self.lock.acquire()
                self.resource[service][self.zk_servers] = severs
                self.lock.release()

    def watch_clients_exists(self, event):
        logger.info("watch_clients is change:" + str(event))
        type = event.type
        path = event.path
        service = path.split("/")[-2]
        if self.zk.exists(path=path, watch=self.watch_clients_exists):
            if type == 'CREATED':
                clients = {}
                client_list = self.zk.get_children(path="%s/%s/%s/" % (self.zk_root, service, self.zk_clients),
                                                   watch=self.watch_clients)
                for client in client_list:
                    client_data_dict = {}
                    client_data = self.zk.get(path="%s/%s/%s/%s" % (self.zk_root, service, self.zk_clients, client))
                    try:
                        client_data_str = safe_list_get(client_data, 0, "{}")
                        client_data_dict = json.loads(client_data_str)
                    except Exception, e:
                        logger.error(
                            'manage.ZKWrap.watch_clients_exists getdata:' + service + "/" + client + ":" + getExceptionDesc(
                                e))
                        pass

                    client_node_stat = safe_list_get(client_data, 1, None)
                    if client_node_stat:
                        client_data_dict['ctime'] = time.strftime("%Y-%m-%d %H:%M:%S",
                                                                  time.localtime(int(client_node_stat.ctime / 1000)))

                    client_acl = False
                    result = self.get_acls("%s/%s/%s/%s" % (self.zk_root, service, self.zk_clients, client))
                    acls = safe_list_get(result, 0, [])
                    if self.zk_harpc_acl in acls:
                        client_acl = True

                    clients[client] = {"data": client_data_dict, "acl": client_acl}

                self.lock.acquire()
                self.resource[service][self.zk_clients] = clients
                self.lock.release()

    def flush_cache(self, type='auto'):
        """
        手动刷新缓存控制
        :param type:
        :return:
        """
        now = time.time()
        time_def = now - self.zk_flush
        if type == 'auto' and time_def > settings.AUTO_CACHE_FLUSH_TIME:
            logger.info('auto flush cache')
            self.zk_flush = now
            self.load_zk_resource()
            return (True,)
        elif type != 'auto' and time_def > settings.CACHE_FLUSH_TIME:
            logger.info('flush cache')
            self.zk_flush = now
            self.load_zk_resource()
            return (True,)
        else:
            logger.info('flush cache between too near')
            return (False, '距离上次刷新时间太近')
