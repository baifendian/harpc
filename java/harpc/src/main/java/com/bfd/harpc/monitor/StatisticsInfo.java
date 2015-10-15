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

/**
 * 统计信息
 * <p>
 * 
 * @author : dsfan
 * @date : 2015-6-2
 */
public class StatisticsInfo {
    /** 成功次数 */
    private long success;

    /** 失败次数 */
    private long failure;

    /** 每秒请求次数 */
    private float qps;

    /** 最大响应时间 */
    private long maxtime;

    /** 最小响应时间 */
    private long mintime;

    /** 平均响应时间 */
    private float avgtime;

    /** 时间戳 */
    private String time;

    /**
     * getter method
     * 
     * @see StatisticsInfo#success
     * @return the success
     */
    public long getSuccess() {
        return success;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#success
     * @param success
     *            the success to set
     */
    public void setSuccess(long success) {
        this.success = success;
    }

    /**
     * getter method
     * 
     * @see StatisticsInfo#failure
     * @return the failure
     */
    public long getFailure() {
        return failure;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#failure
     * @param failure
     *            the failure to set
     */
    public void setFailure(long failure) {
        this.failure = failure;
    }

    /**
     * getter method
     * 
     * @see StatisticsInfo#qps
     * @return the qps
     */
    public float getQps() {
        return qps;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#qps
     * @param qps
     *            the qps to set
     */
    public void setQps(float qps) {
        this.qps = qps;
    }

    /**
     * getter method
     * 
     * @see StatisticsInfo#maxtime
     * @return the maxtime
     */
    public long getMaxtime() {
        return maxtime;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#maxtime
     * @param maxtime
     *            the maxtime to set
     */
    public void setMaxtime(long maxtime) {
        this.maxtime = maxtime;
    }

    /**
     * getter method
     * 
     * @see StatisticsInfo#mintime
     * @return the mintime
     */
    public long getMintime() {
        return mintime;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#mintime
     * @param mintime
     *            the mintime to set
     */
    public void setMintime(long mintime) {
        this.mintime = mintime;
    }

    /**
     * getter method
     * 
     * @see StatisticsInfo#avgtime
     * @return the avgtime
     */
    public float getAvgtime() {
        return avgtime;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#avgtime
     * @param avgtime
     *            the avgtime to set
     */
    public void setAvgtime(float avgtime) {
        this.avgtime = avgtime;
    }

    /**
     * getter method
     * 
     * @see StatisticsInfo#time
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * setter method
     * 
     * @see StatisticsInfo#time
     * @param time
     *            the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

}
