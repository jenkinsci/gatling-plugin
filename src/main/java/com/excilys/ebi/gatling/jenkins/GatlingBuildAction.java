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
     * URL followed by "/report".
     *
     * If there are no tokens in the URL after "/report", it will just
     * display the index page for the Build action (which will display a list
     * of available reports with hyperlinks).
     *
     * If there is one token in the URL after "/report" (e.g. "/report/MySimId"),
     * the token is assumed to be the simulation ID of a given simulation, and
     * it will use the "report.jelly" template to render that report.
     *
     * If there is more than one token in the URL after "/report" (e.g.
     * "/report/MySimId/source/index.html"), the code assumes that we're rendering
     * individual HTML / CSS / image components of the report, so it basically
     * passes through to the files on disk in the archived report directory.
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doReport(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {

        String rest = request.getRestOfPath();
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }

        // break apart the URL to determine what we're rendering
        String[] tokens = rest.split("/");
        String simName;
        switch (tokens.length) {
            case 1:
                // if there's only one token, it's either an empty string
                // or a sim name.
                simName = tokens[0];
                if (simName.equals("")) {
                    // if it's the empty string, then someone browsed directly
                    // to "/report" for this build, so we will just show
                    // the index page for the build action.
                    ForwardToView forward = new ForwardToView(this, "index.jelly");
                    forward.generateResponse(request, response, this);
                    break;
                } else {
                    // otherwise we have a sim name, so we'll render the jelly
                    // template for an individual report
                    ForwardToView forward = new ForwardToView(this, "report.jelly")
                            .with("simName", simName);
                    forward.generateResponse(request, response, this);
                    break;
                }
            default:
                // if we get here then we're probably trying to render some
                // actual report content
                simName = tokens[0];
                String subItem = tokens[1];

                // for now, we only support "source" as the next item in the
                // URL after "/report/MySimId".  If they requested something
                // else, we just return because we're not currently capable of
                // rendering anything else.  We might want to log a warning or
                // something.
                if (!subItem.equals("source")) return;

                // If we get here, we know we're trying to render some actual
                // HTML/CSS/image component of a report.

                // First we find the particular simulation that is being requested
                BuildSimulation sim = this.getSimulation(simName);

                // This part is nasty and I'd like to find a better way to handle
                // it, but I haven't been able to find anything yet.  The problem
                // is that the Stapler `DirectoryBrowserSupport` (which we're
                // using to basically pass through directly to files in the archived
                // report directory on disk) needs for the `request.getRestOfPath()`
                // method to return a base path that maps to files that actually
                // exist in our report directory.  In our case, we really want that
                // to just be something like "/index.html", but it currently
                // contains something like "MySimId/source/index.html".  So we
                // need to tweak the request to remove the tokens that we've already
                // handled in our code.  This is easy to do if we cast the
                // request object to its actual type, but that's probably not
                // a supported API and could break in the future.
                RequestImpl reqImpl = (RequestImpl)request;
                // advance past the "MySimId" token
                reqImpl.tokens.next();
                // advance past the "source" token
                reqImpl.tokens.next();

                // Now our request has the appropriate relative path and
                // we can just pass through to `DirectoryBrowserSupport`.
                DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, sim.getSimulationDirectory(), sim.getSimulationName(), null, false);
                dbs.generateResponse(request, response, this);
                break;
        }
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
