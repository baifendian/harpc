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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * harpc相关bean定义转化
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-5-18
 */
public class HarpcBeanDefinitionParser implements BeanDefinitionParser {
    /** 使用的javabean */
    private final Class<?> beanClass;

    /**
     * @param beanClass
     */
    public HarpcBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, beanClass);
    }

    /**
     * 实现{@link#parse}
     * <p>
     * 
     * @param element
     * @param parserContext
     * @param clazz
     * @return {@link BeanDefinition}
     */
    private BeanDefinition parse(Element element, ParserContext parserContext, Class<?> clazz) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(clazz);

        Method[] methods = clazz.getMethods();
        String id = StringUtils.EMPTY;
        for (Method method : methods) {
            if (method.getName().length() > 3 && method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                String attribute = method.getName().substring(3);
                char ch = attribute.charAt(0);
                attribute = Character.toLowerCase(ch) + attribute.substring(1);

                String value = element.getAttribute(attribute);

                if (StringUtils.isNotEmpty(value)) {
                    Type type = method.getParameterTypes()[0];
                    if (type == boolean.class) {
                        beanDefinition.getPropertyValues().addPropertyValue(attribute, Boolean.valueOf(value));
                    } else {
                        if ("ref".equals(attribute) && parserContext.getRegistry().containsBeanDefinition(value)) {
                            beanDefinition.getPropertyValues().addPropertyValue(attribute, new RuntimeBeanReference(value));
                        } else {
                            beanDefinition.getPropertyValues().addPropertyValue(attribute, value);
                            if ("id".equals(attribute)) {
                                id = value;
                            }
                        }
                    }
                }
            }
        }
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

        return beanDefinition;
    }

}
