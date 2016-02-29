# -*- coding: utf-8 -*-
# -----------------------

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
from django.conf import settings
from django.contrib import auth
from django.contrib.auth.decorators import login_required, permission_required
from django.http import JsonResponse
from django.shortcuts import render_to_response, redirect
from django.contrib.auth import login as auth_login, logout
from django.template import RequestContext
import re
from manage.customdecorators import auto_flush_cache
from manage.lib.harpcutils import safe_list_get
from manage.lib.zk_harpc import ZK_HARPC

logger = logging.getLogger('manage.views')


def login(request):
    if request.method == 'GET':
        return render_to_response("login.html", locals(), RequestContext(request))


def ajax_login(request):
    if request.method == 'POST':
        data = request.POST
        username = data.get('username', '')
        password = data.get('password', '')
        remMe = data.get('remMe', None)
        next = data.get('next')
        user = auth.authenticate(username=username, password=password)
        if user:
            auth_login(request, user)
            if remMe and remMe == 'on':
                request.session.set_expiry(1157407407)
            return redirect((next if len(next) > 0 else "/manage/service"), locals(), RequestContext(request))
        else:
            msg = '账号密码错误'
        return render_to_response("login.html", locals(), RequestContext(request))


def userLogout(request):
    """
    退出登录
    :param request:
    :return:
    """
    logout(request)
    return redirect("/", locals(), RequestContext(request))


def base(request):
    """
    测试方法
    :param request:
    :return:
    """
    if request.method == "GET":
        return render_to_response("base.html", locals(), RequestContext(request))


@login_required(login_url='/manage')
@auto_flush_cache()
def service(request):
    """
    service页面
    :param request:
    :return:
    """
    if request.method == "GET":
        return render_to_response("service.html", locals(), RequestContext(request))


@login_required(login_url='/manage')
def ajax_service(request):
    """
    ajax获取serivce数据
    :param request:
    :return:
    """
    if request.method == "POST":
        results = []
        services = ZK_HARPC.get_services()
        for service in services:
            service_name = safe_list_get(service, 0, None)
            isOperate = safe_list_get(service, 1, False)

            servers = ZK_HARPC.get_servers(service_name)
            clients = ZK_HARPC.get_clients(service_name)

            server_list = []
            client_list = []

            for server in servers:
                server_list.append(safe_list_get(server, 0, None))

            for client in clients:
                client_list.append(safe_list_get(client, 0, None))

            results.append([service_name, ",".join(server_list), ",".join(client_list), isOperate])

    return JsonResponse({'data': results}, safe=False)


@login_required(login_url='/manage')
def index(request):
    """
    index页面
    :param request:
    :return:
    """
    if request.method == 'GET':
        data = request.GET
        msg = data.get('msg', None)
        results = ZK_HARPC.get_harpc_tree()
        serviceNum = len(results)
        clientNum = 0
        serversNum = 0
        for (k, v) in results.items():
            clientNum += len(v.get('clients', []))
            serversNum += len(v.get('servers', []))
        return render_to_response("index.html", locals(), RequestContext(request))


@login_required(login_url='/manage')
def ajax_harpc_log(request):
    """
    ajax方式获取数据
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        key = data.get('key', None)
        type = data.get('type', None)
        start = data.get('start', 0)
        end = data.get('end', 0)
        results = []
        if key and type:
            results = ZK_HARPC.get_statistics(service=service, key=key, type=type, start=int(start), end=int(end))
        return JsonResponse(results, safe=False)


@permission_required('manage.admin_task')
def ajax_create_service(request):
    """
    创建service
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        if service and len(service) > 0:
            if not re.match('[\d\w\(\)]*', service):
                return JsonResponse({"rc": 1, 'msg': 'service格式不正确'}, safe=False)
            result = ZK_HARPC.create_service(service=service)
            if result[0]:
                return JsonResponse({"rc": 0, 'msg': result[1]}, safe=False)
            else:
                return JsonResponse({"rc": 1, 'msg': '创建失败'}, safe=False)
        else:
            return JsonResponse({"rc": 1, 'msg': 'service或servers不得为空'}, safe=False)


@permission_required('manage.admin_task')
def ajax_update_server(request):
    """
    跟新一个节点(废弃的方法)
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        servers = data.get('servers', None)

        if (not service or not servers) or not (len(service) > 0 and len(servers) > 0):
            return JsonResponse({"rc": 0, 'msg': 'service/servers不得为空'}, safe=False)

        if not ZK_HARPC.get_service_acl(service):
            return JsonResponse({"rc": 0, 'msg': '您没有操作的权限'}, safe=False)

        server_old_list = []
        server_new_list = servers.split(",")

        servers_olds = ZK_HARPC.get_servers(service)
        for servers_old in servers_olds:
            server_old_name = safe_list_get(servers_old, 0, None)
            if server_old_name:
                server_old_list.append(server_old_name)

        add_servers = [i for i in server_new_list if i not in server_old_list]
        del_servers = [i for i in server_old_list if i not in server_new_list]

        create_result = ZK_HARPC.create_servers(service=service, servers=add_servers)
        delete_result = ZK_HARPC.delete_servers(service=service, servers=del_servers)

        if create_result[0] and delete_result[0]:
            return JsonResponse({"rc": 0, 'msg': '更新成功'}, safe=False)
        else:
            return JsonResponse({"rc": 1, 'msg': '跟新失败'}, safe=False)


@permission_required('manage.admin_task')
def ajax_delete(request):
    """
    删除创建的节点
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        server = data.get('server', None)

        if not ZK_HARPC.get_service_acl(service):
            return JsonResponse({"rc": 1, 'msg': '您没有操作%s的权限' % (service)}, safe=False)

        if service and len(service) > 0:
            if server and len(server) > 0:
                result = ZK_HARPC.delete_server(service=service, server=server)
            else:
                result = ZK_HARPC.delete_service(service=service)

            if result[0]:
                return JsonResponse({"rc": 0, 'msg': '删除成功,请刷新页面查看结果'}, safe=False)
            else:
                return JsonResponse({"rc": 1, 'msg': '删除失败'}, safe=False)
        else:
            return JsonResponse({"rc": 1, 'msg': 'service不得为空'}, safe=False)


@login_required(login_url='/manage')
@auto_flush_cache()
def servers(request):
    """
    servers页面
    :param request:
    :return:
    """
    if request.method == 'GET':
        data = request.GET
        service = data.get('service', None)

        isExists = False
        if service and len(service) > 0:
            isExists = ZK_HARPC.service_exist(service=service)
            isOperate = ZK_HARPC.get_service_acl(service=service)

        if not isExists:
            services = ZK_HARPC.get_services()

        return render_to_response("servers.html", locals(), RequestContext(request))


@login_required(login_url='/manage')
def ajax_servers(request):
    """
    获取server数据
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        results = []
        if service and len(service) > 0:
            servers = ZK_HARPC.get_servers(service=service)
            for server in servers:
                server_name = safe_list_get(server, 0, None)
                isOperate = safe_list_get(server, 1, False)

                server_info_dict = ZK_HARPC.get_service_child_info(service=service, type=settings.ZK_SERVERS,
                                                            child=server_name)

                results.append([server_name, server_info_dict.get('name', None), server_info_dict.get('owner', None),
                                server_info_dict.get('protocol', None), server_info_dict.get('ctime', None), isOperate])

        return JsonResponse({'data': results}, safe=False)


@permission_required('manage.admin_task')
def ajax_create_server(request):
    """
    创建一个server
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        server = data.get('server', None)
        server_name = data.get('server_name', None)
        server_owner = data.get('server_owner', None)
        server_protocol = data.get("server_protocol", None)

        if (not service or not server) or not (len(service) > 0 and len(server) > 0):
            return JsonResponse({"rc": 1, 'msg': 'service/server不得为空'}, safe=False)

        if not (server_name and server_owner and server_protocol):
            return JsonResponse({"rc": 1, 'msg': '服务名称，责任人，底层协议不得为空'}, safe=False)

        if not re.match(
                '^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9]):\d{0,5}$',
                server):
            return JsonResponse({"rc": 1, 'msg': 'server格式不正确，请用ip:port'}, safe=False)

        server_value = json.dumps({'name': server_name, 'owner': server_owner, 'protocol': server_protocol})

        result = ZK_HARPC.create_server(service=service, server=server, server_value=server_value)
        if result[0]:
            return JsonResponse({"rc": 0, 'msg': '创建成功,请刷新页面查看结果'}, safe=False)
        else:
            return JsonResponse({"rc": 1, 'msg': '创建失败'}, safe=False)


@login_required(login_url='/manage')
def clients(request):
    """
    显示clients页面
    :param request:
    :return:
    """
    if request.method == 'GET':
        data = request.GET
        service = data.get("service", None)

        isExists = False
        if service and len(service) > 0:
            isExists = ZK_HARPC.service_exist(service=service)

        if not isExists:
            services = ZK_HARPC.get_services()

        return render_to_response("clients.html", locals(), RequestContext(request))


@login_required(login_url='/manage')
def ajax_clients(request):
    """
    ajax获取客户端数据
    :param request:
    :return:
    """
    if request.method == 'POST':
        data = request.POST
        service = data.get('service', None)
        results = []
        if service and len(service) > 0:
            clients = ZK_HARPC.get_clients(service=service)
            for client in clients:
                client_name = safe_list_get(client, 0, None)
                isOperate = safe_list_get(client, 1, False)

                client_info_dict = ZK_HARPC.get_service_child_info(service=service, type=settings.ZK_CLIENTS,
                                                            child=client_name)

                results.append([client_name, client_info_dict.get('name', None), client_info_dict.get('owner', None),
                                client_info_dict.get('protocol', None), client_info_dict.get('ctime', None), isOperate])

        return JsonResponse({'data': results}, safe=False)


@login_required(login_url='/manage')
@auto_flush_cache()
def getCache(request):
    """
    测试方法
    :param request:
    :return:
    """
    dict = settings.ZK_HARPC.get_resource()
    return JsonResponse({'test': dict}, safe=False)


@login_required(login_url='/manage')
@auto_flush_cache()
def child(request):
    if request.method == 'GET':
        data = request.GET
        service = data.get('service', None)

        isExists = False
        if service and len(service) > 0:
            isExists = ZK_HARPC.service_exist(service=service)
            isOperate = ZK_HARPC.get_service_acl(service=service)

        if not isExists:
            services = ZK_HARPC.get_services()
        else:
            servers_total = ZK_HARPC.get_servers_total(service)
            clients_total = ZK_HARPC.get_clients_total(service)

        return render_to_response("child.html", locals(), RequestContext(request))


@login_required(login_url='/manage')
def flush_cache(request):
    if request.method == 'POST':
        result = ZK_HARPC.flush_cache(type='')
        if result[0]:
            return JsonResponse({'rc': 0, 'msg': '缓存更新成功，请刷新页面！'}, safe=False)
        else:
            return JsonResponse({'rc': 1, 'msg': result[1]}, safe=False)















