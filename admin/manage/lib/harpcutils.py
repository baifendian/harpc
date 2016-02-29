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
import types


__author__ = 'caojingwei'

def safe_list_get (l, idx, default):
  try:
    return l[idx]
  except IndexError:
    return default

def getExceptionDesc(e, key='msg'):
    if isinstance(e, types.InstanceType):
        try:
            msg = ''
            for k in ['info', 'desc', 'matched', ]:
                if k in e.args[0].keys():
                    msg += e.args[0][k] + ' '
            return msg
        except:
            return str(e)
    else:
        return str(e)
