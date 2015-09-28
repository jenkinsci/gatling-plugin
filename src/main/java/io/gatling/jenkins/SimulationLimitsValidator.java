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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class SimulationLimitsValidator {

    private static final int MINIMUM_LIMIT = 0;
    private List<BuildSimulation> simulations;
    private List<SimulationLimits> simulationLimits;
    private PrintStream logger;

    public SimulationLimitsValidator(List<BuildSimulation> simulations, List<SimulationLimits> simulationLimits,
            PrintStream logger) {
        this.simulations = simulations;
        this.simulationLimits = simulationLimits;
        this.logger = logger;
    }

    public boolean isValid() {
        if (simulationLimits == null || simulationLimits.isEmpty()) {
            return true;
        }

        return validateSimutaionsLimits();
    }

    private boolean validateSimutaionsLimits() {
        boolean result = true;

        for (SimulationLimits limits : simulationLimits) {
            BuildSimulation simulation = getSimulationForSimulationLimit(limits.getSimulationName());

            if (simulation != null) {
                logger.println("Validation simulation limits for simultaion: " + simulation.getSimulationName());

                result = isSimulationValid(simulation, limits);

                if (!result) {
                    logger.println("ERROR: Simulation limits failed for simulation: " + simulation.getSimulationName());
                    break;
                }
            } else {
                logger.println(
                        "WARN: No simulation with name: " + limits.getSimulationName() + " found for declared limits");
            }
        }

        return result;
    }

    private boolean isSimulationValid(BuildSimulation simulation, SimulationLimits limits) {
        RequestReport requestReport = simulation.getRequestReport();

        return isFailedReqestsLimitValid(limits, requestReport) && isMaxRequestResponseTimeValid(limits, requestReport)
                && isMeanRequestsResponseTimeValid(limits, requestReport)
                && isMeanNumberOfReuestsPerSecondValid(limits, requestReport);
    }

    private boolean isMeanNumberOfReuestsPerSecondValid(SimulationLimits limits, RequestReport requestReport) {
        logger.println("Validating mean number of requests per second");

        return isInsideMinimumLimits(requestReport.getMeanNumberOfRequestsPerSecond().getTotal(),
                limits.getMeanNumberOfRequestsPerSecond());
    }

    private boolean isMeanRequestsResponseTimeValid(SimulationLimits limits, RequestReport requestReport) {
        logger.println("Validating mean request response times");

        return isInsideMaximumLimits(requestReport.getMeanResponseTime().getTotal(), limits.getMeanResponseTime());
    }

    private boolean isMaxRequestResponseTimeValid(SimulationLimits limits, RequestReport requestReport) {
        logger.println("Validating maximum requests response time");

        return isInsideMaximumLimits(requestReport.getMaxResponseTime().getTotal(), limits.getMaxResponseTime());
    }

    private boolean isFailedReqestsLimitValid(SimulationLimits limits, RequestReport requestReport) {
        logger.println("Validating number of failed requests");

        return isInsideMaximumLimits(requestReport.getNumberOfRequests().getKO(), limits.getMaxFailed());
    }

    private boolean isInsideMaximumLimits(long original, Long limit) {
        return isAbsent(limit) || original <= limit;
    }

    private boolean isInsideMinimumLimits(long original, Long limit) {
        return isAbsent(limit) || original >= limit;
    }

    private boolean isAbsent(Long limit) {
        return limit == null || limit < MINIMUM_LIMIT;
    }

    private BuildSimulation getSimulationForSimulationLimit(final String simulationName) {
        if (simulations != null && simulationName != null && !simulationName.isEmpty()) {
            for (Iterator<BuildSimulation> iter = simulations.iterator(); iter.hasNext();) {
                BuildSimulation item = iter.next();

                if (item.getSimulationName().equals(simulationName)) {
                    return item;
                }
            }
        }

        return null;
    }

}
