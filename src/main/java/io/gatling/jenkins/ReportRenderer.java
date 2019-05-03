/**
 * Copyright 2011-2018 GatlingCorp (http://gatling.io)
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

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * This class is used by the {@link GatlingBuildAction} to handle the rendering
 * of gatling reports.
 */
public class ReportRenderer {

  private Action action;
  private BuildSimulation simulation;
  private final Set<File> safeDirectories;

  public ReportRenderer(Action gatlingBuildAction, BuildSimulation simulation) {
    this.action = gatlingBuildAction;
    this.simulation = simulation;

    File rootDir = simulation.getSimulationDirectory();
    this.safeDirectories = unmodifiableSet(new HashSet<>(asList(
            rootDir,
            new File(rootDir, "js"),
            new File(rootDir, "style")
    )));
  }

  /**
   * This method will be called when there are no remaining URL tokens to
   * process after {@link GatlingBuildAction} has handled the initial
   * `/report/MySimulationName` prefix.  It renders the `report.jelly`
   * template inside of the Jenkins UI.
   *
   * @param request
   * @param response
   * @throws IOException
   * @throws ServletException
   */
  public void doIndex(StaplerRequest request, StaplerResponse response)
    throws IOException, ServletException {
    ForwardToView forward = new ForwardToView(action, "report.jelly")
      .with("simName", simulation.getSimulationName());
    forward.generateResponse(request, response, action);
  }

  /**
   * This method will be called for all URLs that are routed here by
   * {@link GatlingBuildAction} with a prefix of `/source`.
   *
   * All such requests basically result in the servlet simply serving
   * up content files directly from the archived simulation directory
   * on disk.
   *
   * @param request
   * @param response
   * @throws IOException
   * @throws ServletException
   */
  public void doSource(StaplerRequest request, StaplerResponse response)
    throws IOException, ServletException {
    File dir = simulation.getSimulationDirectory();
    String fileName = request.getRestOfPath();
    if (fileName.isEmpty()) {
      // serve the index page
      throw HttpResponses.redirectTo("source/index.html");
    }
    if (fileName.startsWith("/")) {
      fileName = fileName.substring(1);
    }
    File file = new File(dir, fileName);
    if (fileName.endsWith(".html") || safeDirectories.contains(file.getParentFile())){
      try (InputStream in = new FileInputStream(file)) {
        response.serveFile(request, in, file.lastModified(), -1, file.length(), file.getName());
      }
    } else {
      DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(action,
              new FilePath(simulation.getSimulationDirectory()),
              simulation.getSimulationName(), null, false);
      dbs.generateResponse(request, response, action);
    }
  }
}
