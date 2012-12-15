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

import com.excilys.ebi.gatling.jenkins.model.Simulation;
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

public class GatlingPublisher extends Recorder {

	private final Simulation simulation;

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		FilePath reportDirectory = saveFullReport(build.getWorkspace(), build.getRootDir(), simulation.getName());

		SimulationReport report = new SimulationReport(reportDirectory, simulation);
		report.readStatsFile();

		GatlingBuildAction action = new GatlingBuildAction(build, simulation.getName(), report.getGlobalReport(), reportDirectory);

		build.addAction(action);
		return true;
	}

	@DataBoundConstructor
	public GatlingPublisher(Simulation simulation) {
		this.simulation = simulation;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new GatlingProjectAction(project);
	}

	private FilePath saveFullReport(FilePath workspace, File rootDir, String simulationName) throws IOException, InterruptedException {
		FilePath[] files = workspace.list("**/" + simulationName + "*/index.html");
		FilePath parent = files[0].getParent();

		File allSimulationsDirectory = new File(rootDir, "simulations");
		if (!allSimulationsDirectory.exists())
			allSimulationsDirectory.mkdir();

		File simulationDirectory = new File(allSimulationsDirectory, simulationName);
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
