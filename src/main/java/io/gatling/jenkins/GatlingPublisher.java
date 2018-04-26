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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.init.Initializer;
import hudson.init.InitMilestone;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class GatlingPublisher extends Recorder implements SimpleBuildStep {

  private final Boolean enabled;

  @DataBoundConstructor
  public GatlingPublisher(Boolean enabled) {
    this.enabled = enabled;
  }


  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    PrintStream logger = listener.getLogger();
    if (enabled == null) {
      logger.println("Cannot check Gatling simulation tracking status, reports won't be archived.");
      logger.println("Please make sure simulation tracking is enabled in your build configuration !");
      return true;
    }
    if (!enabled) {
      logger.println("Simulation tracking disabled, reports were not archived.");
      return true;
    }

    logger.println("Archiving Gatling reports...");
    FilePath workspace = build.getWorkspace();
    if (workspace != null) {
      List<BuildSimulation> sims = saveFullReports(build, workspace, build.getRootDir(), logger);
      if (sims.isEmpty()) {
        logger.println("No newer Gatling reports to archive.");
        return true;
      }

      GatlingBuildAction action = new GatlingBuildAction(build, sims);

      build.addAction(action);
      return true;
    } else {
      logger.println("Failed to access workspace, it may be on a non-connected slave.");
      return false;
    }
  }

  @Override
  public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener)
          throws InterruptedException, IOException {
    PrintStream logger = listener.getLogger();
    if (enabled == null) {
      logger.println("Cannot check Gatling simulation tracking status, reports won't be archived.");
      logger.println("Please make sure simulation tracking is enabled in your build configuration !");
      return;
    }
    if (!enabled) {
      logger.println("Simulation tracking disabled, reports were not archived.");
      return;
    }

    logger.println("Archiving Gatling reports...");

    List<BuildSimulation> sims = saveFullReports(run, workspace, run.getRootDir(), logger);
    if (sims.isEmpty()) {
      logger.println("No newer Gatling reports to archive.");
      return;
    }

    GatlingBuildAction action = new GatlingBuildAction(run, sims);

    run.addAction(action);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.BUILD;
  }

  private List<BuildSimulation> saveFullReports(@Nonnull Run<?,?> run, @Nonnull FilePath workspace, @Nonnull File rootDir, @Nonnull PrintStream logger)
          throws IOException, InterruptedException {
    FilePath[] files = workspace.list("**/global_stats.json");
    List<FilePath> reportFolders = new ArrayList<FilePath>();

    if (files.length == 0) {
      logger.println("Could not find a Gatling report in results folder.");
      return Collections.emptyList();
    }

    // Get reports folders for all "global_stats.json" found
    for (FilePath file : files) {
      reportFolders.add(file.getParent().getParent());
    }

    List<FilePath> reportsToArchive = selectReports(run, reportFolders, logger);


    // If the most recent report has already been archived, there's nothing else to do
    if (reportsToArchive.isEmpty()) {
      return Collections.emptyList();
    }

    List<BuildSimulation> simsToArchive = new ArrayList<BuildSimulation>();

    File allSimulationsDirectory = new File(rootDir, "simulations");
    if (!allSimulationsDirectory.exists()) {
      boolean mkdirResult = allSimulationsDirectory.mkdir();
      if (! mkdirResult) {
        logger.println("Could not create simulations archive directory '" + allSimulationsDirectory + "'");
        return Collections.emptyList();
      }
    }

    for (FilePath reportToArchive : reportsToArchive) {
      String name = reportToArchive.getName();
      int dashIndex = name.lastIndexOf('-');
      String simulation = name.substring(0, dashIndex);
      File simulationDirectory = new File(allSimulationsDirectory, name);
      boolean mkdirResult = simulationDirectory.mkdir();
      if (! mkdirResult) {
        logger.println("Could not create simulation archive directory '" + simulationDirectory + "'");
        return Collections.emptyList();
      }

      FilePath reportDirectory = new FilePath(simulationDirectory);

      reportToArchive.copyRecursiveTo(reportDirectory);

      SimulationReport report = new SimulationReport(reportDirectory, simulation);
      report.readStatsFile();
      BuildSimulation sim = new BuildSimulation(simulation, report.getGlobalReport(), reportDirectory);

      simsToArchive.add(sim);
    }


    return simsToArchive;
  }

  @Nonnull
  private static List<FilePath> selectReports(@Nonnull Run<?, ?> run, @Nonnull List<FilePath> reportFolders,
                                              @Nonnull PrintStream logger) throws InterruptedException, IOException {
    long buildStartTime = run.getStartTimeInMillis();
    List<FilePath> reportsFromThisBuild = new ArrayList<FilePath>();
    for (FilePath reportFolder : reportFolders) {
      long reportLastMod = reportFolder.lastModified();

      if (reportLastMod > buildStartTime) {
        logger.println("Adding report '" + reportFolder.getName() + "'");
        reportsFromThisBuild.add(reportFolder);
      }
    }
    return reportsFromThisBuild;
  }

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    @Override
    @SuppressWarnings("rawtypes")
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return Messages.title();
    }

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void addAliases() {
      Items.XSTREAM2.addCompatibilityAlias("com.excilys.ebi.gatling.jenkins.GatlingPublisher", GatlingPublisher.class);
      Items.XSTREAM2.addCompatibilityAlias("com.excilys.ebi.gatling.jenkins.GatlingBuildAction", GatlingBuildAction.class);
    }
  }
}
