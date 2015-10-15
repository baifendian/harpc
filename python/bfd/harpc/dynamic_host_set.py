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

from bfd.harpc import settings
ASYNC_TAG = False


class DynamicHostSet(object):
    """dynamic host set"""
    section_name = "loadbalancer"

    def __init__(self, config):
        self._logger = logging.getLogger(__name__)
        self.all_nodes = set()
        self.live_nodes = set()
        self.dead_nodes = set()
        self._onchange = None
        if ASYNC_TAG:
            from gevent.lock import RLock
            self._lock = RLock()
        else:
            from threading import RLock
            self._lock = RLock()

        self._heartbeat_retry = config.getint(DynamicHostSet.section_name, "heartbeat_retry",
                                              default=settings.DEFAULT_HEARTBEAT_RETRY)
        self._heartbeat_time_out = config.getint(DynamicHostSet.section_name, "heartbeat_timeout", default=settings.DEFAULT_HEARTBEAT_TIMEOUT)

    def set_onchange(self, onchange):
        """set onchange func"""
        self._onchange = onchange

    def reset_with_list(self, node_list):
        """reset host set with given list"""
        with self._lock:
            self.all_nodes.clear()
            self.live_nodes.clear()
            self.dead_nodes.clear()
            self.all_nodes.update(node_list)
            self.live_nodes.update(node_list)
            self._onchange(list(self.live_nodes))

    def mark_dead(self, node):
        """mark a node dead"""
        with self._lock:
            if node in self.live_nodes:
                self.live_nodes.discard(node)
                self.dead_nodes.add(node)
                self._onchange(list(self.live_nodes))

    def recover_dead(self, node):
        """recover one dead node"""
        with self._lock:
            if node in self.all_nodes:
                if node in self.dead_nodes:
                    self._logger.info("recover dead nodes:%s" % node)
                    self.dead_nodes.discard(node)
                    self.live_nodes.add(node)
                    self._onchange(list(self.live_nodes))

    def recover_all(self):
        """recover all node"""
        with self._lock:
            self.live_nodes.update(self.dead_nodes)
            self.dead_nodes.clear()
            self._onchange(list(self.live_nodes))

    def heartbeat_check_all(self):
        """check all node state"""
        for node in self.all_nodes.copy():
            self.heartbeat_check(node)

    def heartbeat_check(self, node):
        """check node state"""
        addr = (node.split(":")[0], int(node.split(":")[1]))
        for i in range(settings.DEFAULT_HEARTBEAT_RETRY):
            s = socket.socket()
            s.settimeout(settings.DEFAULT_HEARTBEAT_TIMEOUT)
            try:
                s.connect(addr)
                self._logger.debug("heartbeat check node:%s success" % node)
                if node in self.dead_nodes:
                    self._logger.info("heartbeat check mark dead recover node:%s" % node)
                    self.recover_dead(node)
                break
            except socket.error as e:
                self._logger.warn("heartbeat check node:%s failed msg:%s " % (node, e))
                if i == settings.DEFAULT_HEARTBEAT_RETRY - 1:
                    if node in self.live_nodes:
                        self._logger.warn("heartbeat check mark dead add node:%s" % node)
                        self.mark_dead(node)
            finally:
                s.close()
