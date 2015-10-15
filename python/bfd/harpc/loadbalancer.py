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
import time
import threading

from bfd.harpc import settings
from bfd.harpc.common import utils


class LoadBalancer(object):
    """loadbalancer"""

    def __init__(self, strategy, dynamic_host_set, config, collector=None):
        self._section_name = utils.get_module(__name__)
        self._logger = logging.getLogger(__name__)
        self._strategy = strategy
        self._collector = collector
        self._heartbeat_interval = config.getint(self._section_name, "heartbeat_interval",
                                                 default=settings.DEFAULT_HEARTBEAT_INTERVAL)
        self._monitor = settings.SERVICE_MONITOR
        self._dynamic_host_set = dynamic_host_set
        self._dynamic_host_set.set_onchange(self._create_hostset_onchange())
        self._result_options = {"SUCCESS": self._success, "TIMEOUT": self._timeout, "DEAD": self._dead,
                                "FAILED": self._failed}

        self.spawn(self._heartbeat)

    def spawn(self, func, *args, **kwargs):
        t = threading.Thread(target=func, args=args, kwargs=kwargs)
        t.daemon = True
        t.start()
        return t

    def _create_hostset_onchange(self):
        """create closure execute on dynamic_host_set change"""

        def onchange(backends):
            self._strategy.offer_backends(backends)

        return onchange

    def get_backend(self):
        """get next backend server node"""
        return self._strategy.get_backend()

    def _mark_dead_backend(self, server_node):
        """mark a servernode dead"""
        self._logger.warn("mark dead backend add host:%s" % server_node)
        self._dynamic_host_set.mark_dead(server_node)
        if len(self._dynamic_host_set.live_nodes) == 0:
            self._logger.info("recover all the nodes")
            self._dynamic_host_set.recover_all()

    def request_result(self, server_node, result_type, request_time):
        """after request do"""
        self._result_options[result_type](server_node, request_time)
        if self._collector:
            self._collector.collect(result_type, request_time)

    def _success(self, server_node, request_time):
        pass

    def _timeout(self, server_node, request_time):
        self._mark_dead_backend(server_node)

    def _dead(self, server_node, request_time):
        self._mark_dead_backend(server_node)

    def _failed(self, server_node, request_time):
        self._mark_dead_backend(server_node)

    def _heartbeat(self):
        """heartbeat check"""
        while True:
            time.sleep(self._heartbeat_interval)
            self._dynamic_host_set.heartbeat_check_all()
