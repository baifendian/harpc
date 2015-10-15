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

import threading

from kazoo.client import KazooClient
from kazoo.retry import KazooRetry

from bfd.harpc.common import utils
from bfd.harpc import settings


class HARpcZKClientManager(KazooClient):
    __client_dict = {}
    __lock = threading.RLock()

    @classmethod
    def make(cls, hosts, config, tag):
        with cls.__lock:
            key = "%s_%s" % (hosts, tag)
            if hosts in cls.__client_dict:
                return cls.__client_dict.get(key)
            else:
                client = cls(hosts, config)
                client.start()
                cls.__client_dict[key] = client
                return client

    def __init__(self, hosts, config):
        self._section_name = utils.get_module(__name__)
        self._max_delay = config.getint(self._section_name, "max_retry_delay",
                                        default=settings.DEFAULT_ZK_RETRY_MAX_DELAY)

        self._timeout = config.getint(self._section_name, "time_out", default=settings.DEFAULT_ZK_CONNECTION_TIMEOUT)
        connection_retry = KazooRetry(max_tries=-1, max_delay=self._max_delay)
        super(HARpcZKClientManager, self).__init__(hosts=hosts, timeout=self._timeout,
                                                   connection_retry=connection_retry)
