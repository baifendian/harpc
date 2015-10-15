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
package com.bfd.harpc.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 统计信息汇总
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-7-22
 */
public class StatisticsTotal {
    /** 总计信息 */
    private StatisticsInfo total;

    /** 详情信息 */
    private List<StatisticsInfo> detail = new ArrayList<StatisticsInfo>();

    /**
     * getter method
     * 
     * @see StatisticsTotal#total
     * @return the total
     */
    public StatisticsInfo getTotal() {
        return total;
    }

    /**
     * setter method
     * 
     * @see StatisticsTotal#total
     * @param total
     *            the total to set
     */
    public void setTotal(StatisticsInfo total) {
        this.total = total;
    }

    /**
     * getter method
     * 
     * @see StatisticsTotal#detail
     * @return the detail
     */
    public List<StatisticsInfo> getDetail() {
        return detail;
    }

    /**
     * setter method
     * 
     * @see StatisticsTotal#detail
     * @param detail
     *            the detail to set
     */
    public void setDetail(List<StatisticsInfo> detail) {
        this.detail = detail;
    }

}
