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

import static com.excilys.ebi.gatling.jenkins.PluginConstants.*;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.DirectoryBrowserSupport;
import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

public class GatlingBuildAction implements Action {

	private final AbstractBuild<?, ?> build;
    private final List<BuildSimulation> simulations;

public GatlingBuildAction(AbstractBuild<?, ?> build, List<BuildSimulation> sims) {
		this.build = build;
        this.simulations = sims;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
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
     * URL followed by "/report/SomeSimulationName".
     *
     * It returns a new instance of {@link ReportRenderer}, which contains
     * the actual logic for rendering a report.
     *
     * @param simName
     */
    public ReportRenderer getReport(String simName) {
        return new ReportRenderer(this, getSimulation(simName));
    }


    public String getReportURL(String simName) {
        return new StringBuilder().append(URL_NAME).append("/report/").append(simName).toString();
    }


    private BuildSimulation getSimulation(String simulationName) {
        // this isn't the most efficient implementation in the world :)
        for (BuildSimulation sim : this.getSimulations()) {
            if (sim.getSimulationName().equals(simulationName)) {
                return sim;
            }
        }
        return null;
    }

}
