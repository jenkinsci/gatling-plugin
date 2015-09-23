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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class SimulationLimitsValidatorTest {

	@Test
	public void testValidScenario() {
		String simulationName = "simulation";
		SimulationLimits simulationLimits = createSimulationLimits(simulationName, 25L, null, 0L, 3000L);

		Statistics meanNumberOfRequestsPerSecond = createStatistics(200, 10000);
		Statistics meanResponseTime = createStatistics(20, 100);
		Statistics numberOfRequests = createStatistics(20, 100);

		RequestReport requestReport = createRequestReport(new Statistics(), meanNumberOfRequestsPerSecond, meanResponseTime, numberOfRequests);
		BuildSimulation buildSimulation = new BuildSimulation(simulationName, requestReport, null);
		SimulationLimitsValidator validator = new SimulationLimitsValidator(Arrays.asList(buildSimulation), Arrays.asList(simulationLimits), System.out);

		Assert.assertTrue(validator.isValid());
	}

	@Test
	public void testValidScenario2() {
		String simulationName = "simulation";
		SimulationLimits simulationLimits = createSimulationLimits(simulationName, 25L, null, 0L, 3000L);
		SimulationLimits simulationLimits2 = createSimulationLimits("simulation2", 25L, null, 0L, 3000L);

		Statistics meanNumberOfRequestsPerSecond = createStatistics(200, 10000);
		Statistics meanResponseTime = createStatistics(20, 100);
		Statistics numberOfRequests = createStatistics(20, 100);

		RequestReport requestReport = createRequestReport(new Statistics(), meanNumberOfRequestsPerSecond, meanResponseTime, numberOfRequests);
		BuildSimulation buildSimulation = new BuildSimulation(simulationName, requestReport, null);
		SimulationLimitsValidator validator = new SimulationLimitsValidator(Arrays.asList(buildSimulation),
				Arrays.asList(simulationLimits, simulationLimits2), System.out);

		Assert.assertTrue(validator.isValid());
	}

	@Test
	public void testInvalidScenario() {
		String simulationName = "simulation";
		SimulationLimits simulationLimits = createSimulationLimits(simulationName, 25L, null, 0L, 3000000L);

		Statistics meanNumberOfRequestsPerSecond = createStatistics(200, 10000);
		Statistics meanResponseTime = createStatistics(20, 100);
		Statistics numberOfRequests = createStatistics(20, 100);

		RequestReport requestReport = createRequestReport(new Statistics(), meanNumberOfRequestsPerSecond, meanResponseTime, numberOfRequests);
		BuildSimulation buildSimulation = new BuildSimulation(simulationName, requestReport, null);
		SimulationLimitsValidator validator = new SimulationLimitsValidator(Arrays.asList(buildSimulation), Arrays.asList(simulationLimits), System.out);

		Assert.assertFalse(validator.isValid());
	}

	@Test
	public void testInvalidScenario2() {
		String simulationName = "simulation";
		SimulationLimits simulationLimits = createSimulationLimits(simulationName, 2L, null, 0L, 300L);

		Statistics meanNumberOfRequestsPerSecond = createStatistics(200, 10000);
		Statistics meanResponseTime = createStatistics(20, 100);
		Statistics numberOfRequests = createStatistics(20, 100);

		RequestReport requestReport = createRequestReport(new Statistics(), meanNumberOfRequestsPerSecond, meanResponseTime, numberOfRequests);
		BuildSimulation buildSimulation = new BuildSimulation(simulationName, requestReport, null);
		SimulationLimitsValidator validator = new SimulationLimitsValidator(Arrays.asList(buildSimulation), Arrays.asList(simulationLimits), System.out);

		Assert.assertFalse(validator.isValid());
	}

	private RequestReport createRequestReport(Statistics maxRespTime, Statistics meanNumberOfReqPerSec, Statistics meanRespTime, Statistics numberOfReq) {
		RequestReport requestReport = new RequestReport();
		requestReport.setMaxResponseTime(maxRespTime);
		requestReport.setMeanNumberOfRequestsPerSecond(meanNumberOfReqPerSec);
		requestReport.setNumberOfRequests(numberOfReq);
		requestReport.setMeanResponseTime(meanRespTime);

		return requestReport;
	}

	private Statistics createStatistics(long ok, long ko) {
		Statistics meanNumberOfRequestsPerSecond = new Statistics();
		meanNumberOfRequestsPerSecond.setKO(ok);
		meanNumberOfRequestsPerSecond.setOK(ko);
		meanNumberOfRequestsPerSecond.setTotal(ok + ko);

		return meanNumberOfRequestsPerSecond;
	}

	private SimulationLimits createSimulationLimits(String simulationName, Long maxFailed, Long maxRespTime, Long meanRespTime, Long numberOfReqsPerSec) {
		SimulationLimits simulationLimits = new SimulationLimits();
		simulationLimits.setSimulationName(simulationName);
		simulationLimits.setMaxFailed(maxFailed);
		simulationLimits.setMaxResponseTime(maxRespTime);
		simulationLimits.setMeanResponseTime(meanRespTime);
		simulationLimits.setMeanNumberOfRequestsPerSecond(numberOfReqsPerSec);

		return simulationLimits;
	}

}
