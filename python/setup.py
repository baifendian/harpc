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

from setuptools import setup

with open('requirements.txt') as f:
    required = f.read().splitlines()

setup(
    name="bfd",
    version="1.1.0",
    author="ruoshui.jin",
    author_email="ruoshui.jin@baifendian.com",
    description="harpc-python",
    install_requires=required,
    packages=["bfd.harpc", "bfd.harpc.common", "bfd.harpc.loadbalancing_strategy", "bfd.harpc.thrift_server"]
)
