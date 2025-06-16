/**
 * Copyright 2011-2020 GatlingCorp (http://gatling.io)
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

import hudson.util.IOUtils;
import javax.servlet.ServletOutputStream;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class is used to download the zipped Reports file
 */
public class ReportDownloader {

  private BuildSimulation simulation;

  public ReportDownloader(BuildSimulation simulation) {
    this.simulation = simulation;
  }

  /**
   * This method will be called when the user clicks on the Gatling reports link
   *
   * @param request
   * @param response
   * @throws IOException
   * @throws InterruptedException
   */
  @SuppressWarnings("unused")
  public void doIndex(StaplerRequest request, StaplerResponse response)
      throws IOException, InterruptedException {
    File file = ZipSimulationUtil.getSimulationZip(simulation.getSimulationDirectory());

    response.setContentType("application/zip");
    response.setContentLength((int) file.length());
    response.addHeader("Content-Disposition", "attachment;filename=\"" + simulation.getSimulationName() + ".zip\"");

    try (ServletOutputStream os = response.getOutputStream();
         FileInputStream fis = new FileInputStream(file)) {
      IOUtils.copy(fis, os);
    }
  }
}
