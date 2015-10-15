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

import json
import time
import logging
import threading
import multiprocessing
from bfd.harpc import settings
from bfd.harpc.common import utils


class StatisticsCollector(object):
    """collect status info and send to zk"""

    def __init__(self, zkclient, config, is_server=False):
        self._section_name = utils.get_module(__name__)
        self._logger = logging.getLogger(__name__)
        self._zkclient = zkclient
        self._is_server = is_server
        self.queue = multiprocessing.Queue()
        self._info = {}
        self._current_info = StatisticsInfo()
        self._total_info = StatisticsInfo()
        self._lock = threading.RLock()
        self._path = None
        self._start_time = None
        self._last_update_time = None
        self.__class__.collect = self.__class__._update_info
        self._collect_interval = config.getint(self._section_name, "interval",
                                               default=settings.DEFAULT_SEND_INFO_INTERVAL)
        self._collect_nodes = config.getint(self._section_name, "node_num",
                                            default=settings.DEFAULT_MAX_DETAILINFO_NODE_NUM)
        if self._is_server:
            self.__class__.collect = self.__class__._server_collect

    @property
    def interval(self):
        return self._collect_interval

    def _format_time_info(self, current_info):
        keys = ["maxtime", "mintime", "avgtime"]
        for key,value in current_info.items():
            if key in keys:
                current_info[key] = value * 1000

    def _send_info(self):
        """send info to zk"""
        while True:
            time.sleep(self._collect_interval)
            self._logger.debug("send collector msg to zk")
            with self._lock:
                total_info = self._total_info.get_dict()
                self._format_time_info(total_info)
                current_info = self._current_info.get_dict()
                self._format_time_info(current_info)
                self._current_info.reset()
            try:
                current_timestamp = time.time()
                current_time = time.strftime("%Y%m%d%H%M%S", time.localtime(current_timestamp))
                total_info["time"] = current_time
                total_info["qps"] = (total_info["success"] + total_info["failure"]) / (
                    current_timestamp - self._start_time)
                self._info["total"] = total_info
                current_info["time"] = current_time
                current_info["qps"] = (current_info["success"] + current_info["failure"]) / (
                    current_timestamp - self._last_update_time)
                detail = self._info.get("detail", [])
                detail.append(current_info)
                while len(detail) > self._collect_nodes:
                    del detail[0]
                self._info["detail"] = detail
                if not self._zkclient.exists(self._path):
                    self._zkclient.create(self._path, ephemeral=True)
                self._zkclient.set(self._path, json.dumps(self._info))
                self._last_update_time = current_timestamp
            except Exception, e:
                self._logger.exception(e)

    def start(self):
        self._start_time = time.time()
        self._last_update_time = self._start_time
        t1 = threading.Thread(target=self._send_info)
        t1.setDaemon(True)
        t1.start()
        if self._is_server:
            t2 = threading.Thread(target=self._server_update_info)
            t2.setDaemon(True)
            t2.start()

    def set_path(self, path):
        self._path = path

    def collect(self, result_type, req_time):
        pass

    def _server_collect(self, result_type, req_time):
        self.queue.put((result_type, req_time))

    def _server_update_info(self):
        while True:
            (result_type, req_time) = self.queue.get()
            self._update_info(result_type, req_time)

    def _update_info(self, result_type, req_time):
        with self._lock:
            if result_type == "SUCCESS":
                self._current_info.success += 1
                self._total_info.success += 1
            else:
                self._current_info.failure += 1
                self._total_info.failure += 1
            if req_time < self._current_info.mintime or self._current_info.mintime == 0:
                self._current_info.mintime = req_time
            if req_time > self._current_info.maxtime:
                self._current_info.maxtime = req_time
            if req_time < self._total_info.mintime or self._total_info.mintime == 0:
                self._total_info.mintime = req_time
            if req_time > self._total_info.maxtime:
                self._total_info.maxtime = req_time
            current_req_num = self._current_info.success + self._current_info.failure
            total_req_num = self._total_info.success + self._total_info.failure
            self._current_info.avgtime = ((current_req_num - 1) * self._current_info.avgtime + req_time) / current_req_num
            self._total_info.avgtime = ((total_req_num - 1) * self._total_info.avgtime + req_time) / total_req_num


class StatisticsInfo(object):
    """statistics info class"""

    def __init__(self):
        self.success = 0
        self.failure = 0
        self.maxtime = 0
        self.mintime = 0
        self.avgtime = 0

    def reset(self):
        self.__init__()

    def get_dict(self):
        return self.__dict__.copy()
