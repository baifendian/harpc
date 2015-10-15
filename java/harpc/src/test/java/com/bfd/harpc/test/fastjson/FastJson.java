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
package com.bfd.harpc.test.fastjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-7-6
 */
public class FastJson {
    public static void main(String[] args) {
        MyObject object = new MyObject();
        object.setId("123");
        object.setName("dsfan");
        MyObjectList list = new MyObjectList();
        HashMap<String, MyObject> map = new HashMap<String, MyObject>();
        map.put(String.valueOf(System.currentTimeMillis()), object);
        list.getList().add(map);
        System.out.println(JSON.toJSON(list));
    }

}

class MyObjectList {
    private List<Map<String, MyObject>> list = new ArrayList<Map<String, MyObject>>();

    /**
     * getter method
     * 
     * @see MyObjectList#list
     * @return the list
     */
    public List<Map<String, MyObject>> getList() {
        return list;
    }

    /**
     * setter method
     * 
     * @see MyObjectList#list
     * @param list
     *            the list to set
     */
    public void setList(List<Map<String, MyObject>> list) {
        this.list = list;
    }

}

class MyObject {
    @JSONField(serialize = false)
    private String id;

    private String name;

    /**
     * getter method
     * 
     * @see com.bfd.harpc.test.fastjson.MyObject#id
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * setter method
     * 
     * @see com.bfd.harpc.test.fastjson.MyObject#id
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getter method
     * 
     * @see com.bfd.harpc.test.fastjson.MyObject#name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * setter method
     * 
     * @see com.bfd.harpc.test.fastjson.MyObject#name
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
