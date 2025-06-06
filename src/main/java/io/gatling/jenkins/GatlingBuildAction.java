/**
 * Copyright 2011-2020 GatlingCorp (http://gatling.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.jenkins;

import static io.gatling.jenkins.PluginConstants.*;

import hudson.model.Action;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jenkins.tasks.SimpleBuildStep;

/**
 * An Action to add to a project/job's build/run page in the UI.
 *
 * <p>Note that in order to be compatible with the new Jenkins Pipeline jobs, Actions that should be
 * added to the project/job's page directly must be added by implementing the
 * SimpleBuildStep.LastBuildAction interface, and encapsulating the project actions in the build
 * action via the getProjectActions method.
 *
 * <p>This is necessary and now preferred to the old approach of defining getProjectAction directly
 * on the Publisher, because for a Pipeline job, Jenkins doesn't know ahead of time what actions
 * will be triggered, and will never call the Publisher.getProjectAction method. Attaching it as a
 * LastBuildAction means that it is discoverable once the Pipeline job has been run once.
 */
public class GatlingBuildAction implements Action, SimpleBuildStep.LastBuildAction {

  private final Run<?, ?> run; // TODO make transient, implement RunAction2
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
   * This method is called dynamically for any HTTP request to our plugin's URL followed by
   * "/report/reportName".
   *
   * <p>It returns a new instance of {@link ReportViewer}, which contains the actual logic for
   * downloading a report.
   *
   * @param reportName the name of the reportName
   */
  @SuppressWarnings("unused")
  public ReportViewer getReport(String reportName) {
    BuildSimulation simulation = getSimulationByReportName(reportName);
    return simulation != null ? new ReportViewer(simulation) : null;
  }

  @SuppressWarnings("unused")
  public ReportDownloader getDownload(String reportName) {
    BuildSimulation simulation = getSimulationByReportName(reportName);
    return simulation != null ? new ReportDownloader(simulation) : null;
  }

  @SuppressWarnings("unused")
  public String getReportURL(BuildSimulation simulation) {
    return URL_NAME + "/report/" + simulation.getSimulationDirectory().getName();
  }

  @SuppressWarnings("unused")
  public String getDownloadURL(BuildSimulation simulation) {
    return URL_NAME + "/download/" + simulation.getSimulationDirectory().getName();
  }

  private BuildSimulation getSimulationByReportName(String reportName) {
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
