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

"""
common config
"""
# zk_path_setting
DEFAULT_ZK_NAMESPACE_SERVERS = "servers"
DEFAULT_ZK_NAMESPACE_ROOT = "harpc"
DEFAULT_ZK_NAMESPACE_STATISTICS = "statistics"
DEFAULT_ZK_NAMESPACE_CLIENTS = "clients"
ZK_AUTH_USER = "admin"
ZK_AUTH_PASSWORD = "admin123"

# zk connect settings
DEFAULT_ZK_CONNECTION_TIMEOUT = 15  # unit s
DEFAULT_ZK_RETRY_MAX_DELAY = 60

# monitor statistics settings
SERVICE_MONITOR = "False"
DEFAULT_SEND_INFO_INTERVAL = 60  # unit s
DEFAULT_MAX_DETAILINFO_NODE_NUM = 600

# accelerate settings
USE_C_MODULE_SERIALIZE = "True"


"""
server config
"""

# server settings
DEFAULT_PROCESS_NUM = 10
DEFAULT_COROUTINES_NUM = 100


"""
    client config
"""
# service info

DEFAULT_REQUEST_RETRY = 3
DEFAULT_REQUEST_TIMEOUT = 3  # unit s
DEFAULT_MARK_DEAD_INTERVAL = 10  # unit s
DEFAULT_HEARTBEAT_RETRY = 3
DEFAULT_HEARTBEAT_TIMEOUT = 3  # unit s
DEFAULT_HEARTBEAT_INTERVAL = 10  # unit s
DEFAULT_USE_ZK = "True"
LOAD_BALANCE_PATH = "bfd.harpc.loadbalancing_strategy.round_robin_strategy.RoundRobinStrategy"

# connection pool settings
DEFAULT_POOL_SIZE = 100
DEFAULT_POOL_TIMEOUT = 3  # unit s
DEFAULT_POOL_MAX_WAIT = 2  # unit s
