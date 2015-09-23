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

public class SimulationLimits {

	private String simulationName;
	private Long maxFailed;
	private Long maxResponseTime;
	private Long meanNumberOfRequestsPerSecond;
	private Long meanResponseTime;

	public String getSimulationName() {
		return simulationName;
	}

	public void setSimulationName(String simulationName) {
		this.simulationName = simulationName;
	}

	public Long getMaxFailed() {
		return maxFailed;
	}

	public void setMaxFailed(Long maxFailed) {
		this.maxFailed = maxFailed;
	}

	public Long getMaxResponseTime() {
		return maxResponseTime;
	}

	public void setMaxResponseTime(Long maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

	public Long getMeanNumberOfRequestsPerSecond() {
		return meanNumberOfRequestsPerSecond;
	}

	public void setMeanNumberOfRequestsPerSecond(Long meanNumberOfRequestsPerSecond) {
		this.meanNumberOfRequestsPerSecond = meanNumberOfRequestsPerSecond;
	}

	public Long getMeanResponseTime() {
		return meanResponseTime;
	}

	public void setMeanResponseTime(Long meanResponseTime) {
		this.meanResponseTime = meanResponseTime;
	}

}
