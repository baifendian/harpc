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

import os
import gevent
import gevent.monkey

__all__ = ['patch_all',
           'patch_thrift',
           'patch_loadbalancer',
           'patch_gevent']

saved = {}

def is_module_patched(modname):
    """Check if a module has been replaced with a cooperative version."""
    return modname in saved


def patch_thrift():
    import gevent.socket
    from thrift.transport import TSocket
    os.environ['GEVENT_RESOLVER'] = 'ares'
    reload(gevent.hub)
    # trans thrift socket to gevent socket
    TSocket.socket = gevent.socket


def patch_item(module, attr, newitem):
    NONE = object()
    olditem = getattr(module, attr, NONE)
    if olditem is not NONE:
        saved.setdefault(module.__name__, {}).setdefault(attr, olditem)
    setattr(module, attr, newitem)


def patch_loadbalancer():
    """Replace :func: spawn with :func:`gevent.spawn`."""
    from gevent import spawn
    from bfd.harpc.loadbalancer import LoadBalancer
    patch_item(LoadBalancer, 'spawn', spawn)
    gevent.monkey.patch_time()


def patch_gevent():
    from bfd.harpc import connection_pool
    connection_pool.ASYNC_TAG = True
    from bfd.harpc import dynamic_host_set
    dynamic_host_set.ASYNC_TAG = True


def patch_all():
    patch_thrift()
    patch_loadbalancer()
    patch_gevent()
