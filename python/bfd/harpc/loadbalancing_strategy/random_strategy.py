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

import random

from bfd.harpc.loadbalancing_strategy import LoadBalanceStrategyBase


class RandomStrategy(LoadBalanceStrategyBase):
    def __init__(self):
        pass

    def offer_backends(self, backend):
        self._backend = backend[:]

    def get_backend(self):
        if len(self._backend) == 0:
            raise Exception("no backends")
        return random.choice(self._backend)
