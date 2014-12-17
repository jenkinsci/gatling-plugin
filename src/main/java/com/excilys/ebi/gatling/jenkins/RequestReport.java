/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestReport {

    private String name;
    private Statistics numberOfRequests;
    private Statistics minResponseTime;
    private Statistics maxResponseTime;
    private Statistics meanResponseTime;
    private Statistics standardDeviation;
    @JsonProperty("percentiles1")
    private Statistics percentiles50;
    @JsonProperty("percentiles2")
    private Statistics percentiles75;
    @JsonProperty("percentiles3")
    private Statistics percentiles95;
    @JsonProperty("percentiles4")
    private Statistics percentiles99;
    private Statistics meanNumberOfRequestsPerSecond;
    private ResponseTimeGroup group1;
    private ResponseTimeGroup group2;
    private ResponseTimeGroup group3;
    private ResponseTimeGroup group4;

    public Statistics getNumberOfRequests() {
        return numberOfRequests;
    }

    public void setNumberOfRequests(Statistics numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Statistics getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(Statistics minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public Statistics getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(Statistics maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public Statistics getMeanResponseTime() {
        return meanResponseTime;
    }

    public void setMeanResponseTime(Statistics meanResponseTime) {
        this.meanResponseTime = meanResponseTime;
    }

    public Statistics getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Statistics standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Statistics getPercentiles50() {
        return percentiles50;
    }

    public void setPercentiles50(Statistics percentiles50) {
        this.percentiles50 = percentiles50;
    }

    public Statistics getPercentiles75() {
        return percentiles75;
    }

    public void setPercentiles75(Statistics percentiles75) {
        this.percentiles75 = percentiles75;
    }

    public Statistics getPercentiles95() {
        return percentiles95;
    }

    public void setPercentiles95(Statistics percentiles95) {
        this.percentiles95 = percentiles95;
    }

    public Statistics getPercentiles99() {
        return percentiles99;
    }

    public void setPercentiles99(Statistics percentiles99) {
        this.percentiles99 = percentiles99;
    }

    public Statistics getMeanNumberOfRequestsPerSecond() {
        return meanNumberOfRequestsPerSecond;
    }

    public void setMeanNumberOfRequestsPerSecond(Statistics meanNumberOfRequestsPerSecond) {
        this.meanNumberOfRequestsPerSecond = meanNumberOfRequestsPerSecond;
    }

    public ResponseTimeGroup getGroup1() {
        return group1;
    }

    public void setGroup1(ResponseTimeGroup group1) {
        this.group1 = group1;
    }

    public ResponseTimeGroup getGroup2() {
        return group2;
    }

    public void setGroup2(ResponseTimeGroup group2) {
        this.group2 = group2;
    }

    public ResponseTimeGroup getGroup3() {
        return group3;
    }

    public void setGroup3(ResponseTimeGroup group3) {
        this.group3 = group3;
    }

    public ResponseTimeGroup getGroup4() {
        return group4;
    }

    public void setGroup4(ResponseTimeGroup group4) {
        this.group4 = group4;
    }
}
