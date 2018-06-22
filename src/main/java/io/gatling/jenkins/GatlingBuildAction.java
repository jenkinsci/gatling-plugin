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
package io.gatling.jenkins;

import static io.gatling.jenkins.PluginConstants.*;

import hudson.model.Action;
import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An Action to add to a project/job's build/run page in the UI.
 *
 * Note that in order to be compatible with the new Jenkins Pipeline jobs,
 * Actions that should be added to the project/job's page directly must be added
 * by implementing the SimpleBuildStep.LastBuildAction interface, and encapsulating
 * the project actions in the build action via the getProjectActions method.
 *
 * This is necessary and now preferred to the old approach of defining getProjectAction
 * directly on the Publisher, because for a Pipeline job, Jenkins doesn't know ahead
 * of time what actions will be triggered, and will never call the Publisher.getProjectAction
 * method.  Attaching it as a LastBuildAction means that it is discoverable once
 * the Pipeline job has been run once.
 */
public class GatlingBuildAction implements Action, SimpleBuildStep.LastBuildAction {

  private final Run<?, ?> run;
  private final List<BuildSimulation> simulations;

  public GatlingBuildAction(Run<?, ?> run, List<BuildSimulation> sims) {
    this.run = run;
    this.simulations = sims;
  }

  public Run<?, ?> getRun() {
    return run;
  }

  public List<BuildSimulation> getSimulations() {
    return simulations;
  }

  public String getIconFileName() {
    return ICON_URL;
  }

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public String getUrlName() {
    return URL_NAME;
  }

  /**
   * This method is called dynamically for any HTTP request to our plugin's
   * URL followed by "/report/reportName".
   *
   * It returns a new instance of {@link ReportRenderer}, which contains the
   * actual logic for rendering a report.
   *
   * @param reportName the name of the reportName
   */
  public ReportRenderer getReport(String reportName) {
    return new ReportRenderer(this, getSimulationByReportName(reportName));
  }

  public String getReportURL(BuildSimulation simulation) {
    return new StringBuilder().append(URL_NAME).append("/report/").append(simulation.getSimulationDirectory().getName()).toString();
  }

  private BuildSimulation getSimulationByReportName(String reportName) {
    // this isn't the most efficient implementation in the world :)
    for (BuildSimulation sim : this.getSimulations()) {
      if (sim.getSimulationDirectory().getName().equals(reportName)) {
        return sim;
      }
    }
    return null;
  }

  @Override
  public Collection<? extends Action> getProjectActions() {
    List<GatlingProjectAction> projectActions = new ArrayList<>();
    projectActions.add(new GatlingProjectAction(run.getParent()));
    return projectActions;
  }
}
