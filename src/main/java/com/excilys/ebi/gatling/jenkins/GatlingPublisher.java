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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GatlingPublisher extends Recorder {

	private final Boolean enabled;
	private AbstractProject<?, ?> project;
    private AbstractBuild<?, ?> build;
	private PrintStream logger;


	@DataBoundConstructor
	public GatlingPublisher(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        this.build = build;
		logger = listener.getLogger();
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
        List<BuildSimulation> sims = saveFullReports(build.getWorkspace(), build.getRootDir());
        if (sims.size() == 0) {
			logger.println("No newer Gatling reports to archive.");
			return true;
		}

        GatlingBuildAction action = new GatlingBuildAction(build, sims);

        build.addAction(action);

        return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
		return new GatlingProjectAction(project);
	}

    private List<BuildSimulation> saveFullReports(FilePath workspace, File rootDir) throws IOException, InterruptedException {
		FilePath[] files = workspace.list("**/global_stats.json");
		List<FilePath> reportFolders = new ArrayList<FilePath>();

		if (files.length == 0) {
			throw new IllegalArgumentException("Could not find a Gatling report in results folder.");
		}

		// Get reports folders for all "global_stats.json" found
		for (FilePath file : files) {
			reportFolders.add(file.getParent().getParent());
		}

        List<FilePath> reportsToArchive = selectReports(reportFolders);


        List<BuildSimulation> simsToArchive = new ArrayList<BuildSimulation>();

		// If the most recent report has already been archived, there's nothing else to do
        if (reportsToArchive.size() == 0) {
			return simsToArchive;
		}

        File allSimulationsDirectory = new File(rootDir, "simulations");
        if (!allSimulationsDirectory.exists())
            allSimulationsDirectory.mkdir();

        for (FilePath reportToArchive : reportsToArchive) {
            String name = reportToArchive.getName();
            int dashIndex = name.lastIndexOf('-');
            String simulation = name.substring(0, dashIndex);
            File simulationDirectory = new File(allSimulationsDirectory, name);
            simulationDirectory.mkdir();

            FilePath reportDirectory = new FilePath(simulationDirectory);

            reportToArchive.copyRecursiveTo(reportDirectory);

            SimulationReport report = new SimulationReport(reportDirectory, simulation);
            report.readStatsFile();
            BuildSimulation sim = new BuildSimulation(simulation, report.getGlobalReport(), reportDirectory);

            simsToArchive.add(sim);
        }


		return simsToArchive;
	}

	private List<FilePath> selectReports(List<FilePath> reportFolders) throws InterruptedException, IOException {
        long buildStartTime = build.getStartTimeInMillis();
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
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.Title();
		}
	}


}
