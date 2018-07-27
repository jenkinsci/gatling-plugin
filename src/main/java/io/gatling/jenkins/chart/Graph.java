/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
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
package io.gatling.jenkins.chart;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.model.Job;
import hudson.model.Run;
import io.gatling.jenkins.GatlingBuildAction;
import io.gatling.jenkins.BuildSimulation;
import io.gatling.jenkins.RequestReport;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Graph<Y extends Number> {
  private static final Logger LOGGER = Logger.getLogger(Graph.class.getName());

  private final SortedMap<SerieName, Serie<Integer, Y>> series = new TreeMap<SerieName, Serie<Integer, Y>>();

  private final ObjectMapper mapper = new ObjectMapper();

  public Graph(Job<?, ?> job, int maxBuildsToDisplay) {
    int numberOfBuild = 0;
    for (Run<?, ?> run : job.getBuilds()) {
      GatlingBuildAction action = run.getAction(GatlingBuildAction.class);

      if (action != null) {
        numberOfBuild++;
        for (BuildSimulation sim : action.getSimulations()) {
          SerieName name = new SerieName(sim.getSimulationName(), sim.getSimulationDirectory().getName());
          if (!series.containsKey(name))
            series.put(name, new Serie<Integer, Y>());

          series.get(name).addPoint(run.getNumber(), getValue(sim.getRequestReport()));
        }
      }

      if (numberOfBuild >= maxBuildsToDisplay)
        break;
    }
  }

  public String getSeriesNamesJSON() {
    String json = null;

    try {
      json = mapper.writeValueAsString(series.keySet());
    } catch (IOException e) {
      LOGGER.log(Level.INFO, e.getMessage(), e);
    }
    return json;
  }

  public String getSeriesJSON() {
    String json = null;

    try {
      json = mapper.writeValueAsString(series.values());
    } catch (IOException e) {
      LOGGER.log(Level.INFO, e.getMessage(), e);
    }
    return json;
  }

  protected abstract Y getValue(RequestReport requestReport);
}
