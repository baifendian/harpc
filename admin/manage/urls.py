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

from django.conf.urls import patterns, url

__author__ = 'caojingwei'


urlpatterns = patterns('manage.views',
                       url(r"^ajax_login$","ajax_login"),
                       url(r"^userLogout$","userLogout"),
                       url(r"^index$", "index"),
                       url(r"^ajax_harpc_log$", "ajax_harpc_log"),
                       url(r"^ajax_create_service$", "ajax_create_service"),
                       url(r"^ajax_update_server$", "ajax_update_server"),
                       url(r"^ajax_delete$", "ajax_delete"),
                       url(r"^base$", "base"),
                       url(r"^service$", "service"),
                       url(r"^ajax_service$", "ajax_service"),
                       url(r"^servers$", "servers"),
                       url(r"^ajax_servers$", "ajax_servers"),
                       url(r"^ajax_create_server$", "ajax_create_server"),
                       url(r"^clients$", "clients"),
                       url(r"^ajax_clients$", "ajax_clients"),
                       url(r"^getCache$", "getCache"),
                       url(r"^child$", "child"),
                       url(r"^flush_cache", "flush_cache"),
                       )