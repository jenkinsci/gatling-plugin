/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package io.gatling.jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class SimulationLimits {

    private final String simulationName;
    private final Long maxFailed;
    private final Long maxResponseTime;
    private final Long meanNumberOfRequestsPerSecond;
    private final Long meanResponseTime;

    @DataBoundConstructor
    public SimulationLimits(String simulationName, Long maxFailed, Long maxResponseTime, Long meanNumberOfRequestsPerSecond, Long meanResponseTime) {
        this.simulationName = simulationName;
        this.maxFailed = maxFailed;
        this.maxResponseTime = maxResponseTime;
        this.meanNumberOfRequestsPerSecond = meanNumberOfRequestsPerSecond;
        this.meanResponseTime = meanResponseTime;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public Long getMaxFailed() {
        return maxFailed;
    }

    public Long getMaxResponseTime() {
        return maxResponseTime;
    }

    public Long getMeanNumberOfRequestsPerSecond() {
        return meanNumberOfRequestsPerSecond;
    }

    public Long getMeanResponseTime() {
        return meanResponseTime;
    }

}
