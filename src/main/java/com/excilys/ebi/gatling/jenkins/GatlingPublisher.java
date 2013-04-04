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

public class GatlingPublisher extends Recorder {

	private final Boolean enabled;
	private String simulation;


	@DataBoundConstructor
	public GatlingPublisher(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		PrintStream logger = listener.getLogger();

		if(enabled == null) {
			logger.println("Cannot check Gatling simulation tracking status, reports won't be archived.");
			logger.println("Please make sure simulation tracking is enabled in your build configuration !");
			return true;
		}
		if(!enabled) {
			logger.println("Simulation tracking disabled, reports were not archived.");
			return true;
		}

		logger.println("Archiving Gatling reports...");
		FilePath reportDirectory = saveFullReport(build.getWorkspace(), build.getRootDir());

		SimulationReport report = new SimulationReport(reportDirectory, simulation);
		report.readStatsFile();

		GatlingBuildAction action = new GatlingBuildAction(build, simulation, report.getGlobalReport(), reportDirectory);

		build.addAction(action);
		return true;
	}

	public String getSimulation() {
		return simulation;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new GatlingProjectAction(project);
	}

	private FilePath saveFullReport(FilePath workspace, File rootDir) throws IOException, InterruptedException {
		FilePath[] files = workspace.list("**/global_stats.json");

		if(files.length == 0) {
			throw new IllegalArgumentException("Could not find a Gatling report in results folder.");
		} else if(files.length != 1) {
			throw new IllegalArgumentException("Found more than one Gatling report in results folder, make sure results folder is cleared between two builds.");
		}

		FilePath parent = files[0].getParent().getParent();
		String name = parent.getName();
		File allSimulationsDirectory = new File(rootDir, "simulations");
		if (!allSimulationsDirectory.exists())
			allSimulationsDirectory.mkdir();
		int dashIndex = name.lastIndexOf('-');
		simulation = name.substring(0,dashIndex);
		File simulationDirectory = new File(allSimulationsDirectory, name);
		simulationDirectory.mkdir();

		FilePath reportDirectory = new FilePath(simulationDirectory);

		parent.copyRecursiveTo(reportDirectory);

		return reportDirectory;
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
