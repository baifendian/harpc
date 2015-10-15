/**
 * Copyright (C) 2015 Baifendian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bfd.harpc.config.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.bfd.harpc.config.RegistryConfig;

/**
 * harpc命名空间处理类
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-18
 */
public class HarpcNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("registry", new HarpcBeanDefinitionParser(RegistryConfig.class));
        registerBeanDefinitionParser("server", new HarpcBeanDefinitionParser(ServerBean.class));
        registerBeanDefinitionParser("client", new HarpcBeanDefinitionParser(ClientBean.class));
    }

}
