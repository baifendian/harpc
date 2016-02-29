# -*- coding: utf-8 -*-

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

from django.utils.six import wraps
from django.utils.decorators import available_attrs
from manage.lib.zk_harpc import ZK_HARPC

__author__ = 'caojingwei'



def auto_flush_cache(type='auto'):
    """
    自动刷新缓存
    """
    actual_decorator = flush_cache(
        lambda u: u.is_authenticated(),
        type=type,
    )
    return actual_decorator


def flush_cache(test_func, type):
    """
    刷新缓存
    """
    def decorator(view_func):
        @wraps(view_func, assigned=available_attrs(view_func))
        def _wrapped_view(request, *args, **kwargs):
            # 登录过的用户自动刷新
            if test_func(request.user):
                ZK_HARPC.flush_cache(type=type)
            return view_func(request, *args, **kwargs)

        return _wrapped_view

    return decorator