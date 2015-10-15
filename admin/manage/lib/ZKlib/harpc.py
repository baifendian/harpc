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
from django.utils.safestring import SafeString
import time
from manage.lib.ZKlib import core
from django.conf import settings
from manage.lib.harpcutils import safe_list_get, getExceptionDesc

__author__ = 'caojingwei'

logger = logging.getLogger('manage.libs')


class HARPC(core.ZKWrap):
    def __del__(self):
        try:
            self.zk.stop()
        except:
            pass

    def commandStat(self):
        return self.zk.command(cmd='stat')

    def get_services(self):
        """
        获取所有services
        :return:
        """
        results = []
        for (k,v) in self.resource.items():
            service_name = k
            service_acl = v.get("acl",False)
            results.append((service_name,service_acl))
        return results


    def get_servers(self, service):
        """
        获取指定service信息
        :param service:
        :return:
        """
        results = []
        try:
            servers_dict = self.resource[service][self.zk_servers]
            for (k,v) in servers_dict.items():
                server_name = k
                server_acl = v.get("acl",False)
                results.append((server_name,server_acl))
        except Exception,e:
            logger.error('manage.get_servers:' + getExceptionDesc(e))
            pass
        return results



    def get_service_acl(self, service):
        """
        获取service权限信息
        :param service:
        :return:
        """
        try:
            return self.resource[service]['acl']
        except Exception,e:
            logger.error('manage.get_service_acl:' + getExceptionDesc(e))
            pass
        return False

    def get_service_child_acl(self,service,type,child):
        """
        获取service孩子节点权限
        :param service:
        :param type:
        :param child:
        :return:
        """
        try:
            return self.resource[service][type][child]['acl']
        except Exception,e:
            logger.error('manage.get_service_child_acl:' + getExceptionDesc(e))
        return False

    def get_clients(self, service):
        """
        获取clients信息
        :param service:
        :return:
        """
        results = []
        try:
            clients_dict = self.resource[service][self.zk_clients]
            for (k,v) in clients_dict.items():
                client_name = k
                client_acl = v.get("acl",False)
                results.append((client_name,client_acl))
        except Exception,e:
            logger.error('manage.get_servers:' + getExceptionDesc(e))
            pass
        return results


    def get_statistics(self, service, key, type, start=0, end=0):
        """
        获取日志
        :param service:
        :param key:
        :param type:
        :param start:
        :param end:
        :return:
        """
        tables = []
        series = []
        statistics_series = settings.ZK_STATISTICS_SERIES
        total = {"avgtime":0,'mintime':0,"maxtime":0,"qps":0.0,"success":0,"failure":0}
        if service and len(service) > 0 and key and len(key) > 0 and type in (self.zk_servers, self.zk_clients):
            try:
                result = self.zk.get("%s/%s/%s/%s/%s/" % (self.zk_root, service, self.zk_statistics, type, key))
                detail_list = {}
                try:
                    result_str = safe_list_get(result,0,"{}")
                    result_dict = json.loads(result_str)
                    detail_list = result_dict['detail']
                    total = result_dict['total']
                except Exception,e:
                    logger.error('manage.harpc.get_statistics getdata:' + getExceptionDesc(e))
                    pass
                for detail in detail_list:
                    date = detail.get("time")
                    if start > 0 and end > 0 and start > int(date) or end < int(date):
                        continue
                    table = [date]
                    for statistics_serie in statistics_series:
                        statistics_serie_name = statistics_serie.get('name', None)
                        value = detail.get(statistics_serie_name, 0)
                        unit = statistics_serie.get('unit', 0)
                        table.append(value)
                        date_struct = time.strptime(SafeString(date), "%Y%m%d%H%M%S")
                        serie_dict = {'name': statistics_serie_name, 'data': [
                                    [date_struct.tm_year, date_struct.tm_mon - 1, date_struct.tm_mday, date_struct.tm_hour,
                                    date_struct.tm_min, date_struct.tm_sec, value]], 'yAxis': unit}
                        for serie in series:
                            if serie.get('name', None) == statistics_serie_name:
                                serie.get('data', []).append(
                                     [date_struct.tm_year, date_struct.tm_mon - 1, date_struct.tm_mday,
                                      date_struct.tm_hour, date_struct.tm_min, date_struct.tm_sec, value])
                                break
                        else:
                             series.append(serie_dict)
                    tables.append(table)
            except Exception,e:
                logger.error('manage.harpc.get_statistics:' + getExceptionDesc(e))
                pass
        results = {'tables': tables, 'series': series,'total':total}
        return results

    def get_harpc_tree(self):
        """
        获取一个harpc树（废弃方法）
        :return:
        """
        result = {}
        services = self.get_services()
        for service in services:
            servers = self.get_servers(service[0])
            clients = self.get_clients(service[0])
            result[service] = {'servers': servers, "clients": clients}
        return result

    def create_service(self, service):
        """
        创建一个service
        :param service:
        :param servers:
        :return:
        """
        try:
            self.zk.create(path='%s/%s' % (self.zk_root, service))
            self.zk.create(path='%s/%s/%s' % (self.zk_root, service,self.zk_servers),acl=[self.zk_harpc_acl, self.zk_anyone_acl])
            self.lock.acquire()
            self.resource[service] = {self.zk_servers: {}, self.zk_clients: {}, "acl": True}
            self.lock.release()
            return (True,"创建成功,请刷新页面查看结果")
        except Exception, e:
            logger.error('manage.create_service:' + getExceptionDesc(e))
            return (False, getExceptionDesc(e))

    def create_service_transaction(self, service, servers):
        """
        创建一个service事务版本
        :param service:
        :param servers:
        :return:
        """
        try:
            self.zk.create(path='%s/%s/%s' % (self.zk_root, service,self.zk_servers), acl=[self.zk_harpc_acl, self.zk_anyone_acl], makepath=True)
            transaction = self.zk.transaction()
            path_list = []
            for server in servers:
                path = path='%s/%s/%s/%s' % (self.zk_root, service, self.zk_servers, server)
                path_list.append(unicode(path))
                transaction.create(path=path,
                           acl=[self.zk_harpc_acl, self.zk_anyone_acl])
            result = transaction.commit()
            if(result == path_list):
                return (True,)
            else:
                logger.error('manage.create_service:' + SafeString(result))
                return (False,"service节点创建成功,但servers节点创建失败！")
        except Exception, e:
            logger.error('manage.create_service:' + getExceptionDesc(e))
            return (False, getExceptionDesc(e))

    def create_server(self,service,server,server_value):
        """
        创建一个server
        :param service:
        :param servers:
        :return:
        """
        try:
            self.zk.create(path='%s/%s/%s/%s' % (self.zk_root, service, self.zk_servers, server),
                            makepath=True,value=server_value)

            self.lock.acquire()
            self.resource[service][self.zk_servers][server] = ({"data": json.loads(server_value), "acl": True})
            self.lock.release()

            return (True,)
        except Exception, e:
            logger.error('manage.create_servers:' + getExceptionDesc(e))
            return (False, getExceptionDesc(e))

    def delete_service(self, service):
        """
        删除一个service
        :param service:
        :param server:
        :return:
        """

        try:
            path = '%s/%s' % (self.zk_root, service)
            self.zk.delete(path=path, recursive=True)

            self.lock.acquire()
            self.resource.pop(service)
            self.lock.release()


            return (True,)
        except Exception, e:
            logger.error('manage.delete_service:' + getExceptionDesc(e))
            return (False, getExceptionDesc(e))

    def delete_server(self,service,server):
        """
        删除一个servers
        :param service:
        :param servers:
        :return:
        """
        try:
            path = '%s/%s/%s/%s' % (self.zk_root, service, self.zk_servers, server)
            self.zk.delete(path=path, recursive=True)

            self.lock.acquire()
            self.resource[service][self.zk_servers].pop(server)
            self.lock.release()

            return (True,)
        except Exception, e:
            logger.error('manage.delect_servers:' + getExceptionDesc(e))
            return (False, getExceptionDesc(e))

    def get_service_child_info(self,service,type,child):
        """
        获取子节点信息
        :param service:
        :param type:
        :param child:
        :return:
        """
        result = {}
        try:
            result = self.resource[service][type][child]['data']
        except Exception,e:
            logger.error('manage.get_service_child_info:' + getExceptionDesc(e))
            pass
        return result

    def service_exist(self,service):
        """
        判断service是否存在
        :param service:
        :return:
        """
        result = self.resource.get(service,None)
        if result:
            return True
        else:
            return False

    def get_servers_total(self,service):
        """
        获取一个servers的合计信息
        :param service:
        :return:
        """
        statistics_series = settings.ZK_STATISTICS_SERIES
        results = {"avgtime":0,'mintime':0,"maxtime":0,"qps":0.0,"success":0,"failure":0}

        try:
            servers = self.resource[service][self.zk_servers]
            for key in servers:
                try:
                    result = self.zk.get(path="%s/%s/%s/%s/%s/" % (self.zk_root, service, self.zk_statistics, self.zk_servers, key))
                    total = {}
                    try:
                        result_str = safe_list_get(result,0,"{}")
                        result_dict = json.loads(result_str)
                        total = result_dict['total']
                    except Exception,e:
                        logger.error('manage.harpc.get_servers_total getdata:' + getExceptionDesc(e))
                        pass
                    for statistics_serie in statistics_series:
                        serie_name = statistics_serie['name']
                        serie_value = total.get(serie_name,0)
                        results[serie_name] = results.get(serie_name,0)+serie_value
                except Exception,e:
                    continue
            for statistics_serie in statistics_series:
                serie_name = statistics_serie['name']
                value = results.get(serie_name,0)
                if serie_name in ['avgtime','mintime','maxtime']:
                    value = value/len(servers)
                if serie_name == 'qps':
                    value = float('%.4f'% value)
                results[serie_name] = value
        except Exception,e:
            logger.error('manage.harpc.get_servers_total:' + getExceptionDesc(e))
            pass
        return results

    def get_clients_total(self,service):
        """
        获取一个clients的合计信息
        :param service:
        :return:
        """
        statistics_series = settings.ZK_STATISTICS_SERIES
        results = {"avgtime":0,'mintime':0,"maxtime":0,"qps":0.0,"success":0,"failure":0}

        try:
            clients = self.resource[service][self.zk_clients]
            for key in clients:
                try:
                    result = self.zk.get(path="%s/%s/%s/%s/%s/" % (self.zk_root, service, self.zk_statistics, self.zk_clients, key))
                    total = {}
                    try:
                        result_str = safe_list_get(result,0,"{}")
                        result_dict = json.loads(result_str)
                        total = result_dict['total']
                    except Exception,e:
                        logger.error('manage.harpc.get_clients_total getdata:' + getExceptionDesc(e))
                        pass
                    for statistics_serie in statistics_series:
                        serie_name = statistics_serie['name']
                        serie_value = total.get(serie_name,0)
                        results[serie_name] = results.get(serie_name,0)+serie_value
                except Exception,e:
                    continue
            for statistics_serie in statistics_series:
                serie_name = statistics_serie['name']
                value = results.get(serie_name,0)
                if serie_name in ['avgtime','mintime','maxtime']:
                    value = value/len(clients)
                if serie_name == 'qps':
                    value = float('%.4f'% value)
                results[serie_name] = value

        except Exception,e:
            logger.error('manage.harpc.get_clients_total:' + getExceptionDesc(e))
            pass
        return results

