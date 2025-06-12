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

import hudson.FilePath;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.File;
import java.io.IOException;

/**
 * This class is used to download the zipped Reports file
 */
public class ReportDownloader {

  private final BuildSimulation simulation;

  public ReportDownloader(BuildSimulation simulation) {
      this.simulation = simulation;
  }

    @SuppressWarnings("unused")
    public void doIndex(HttpServletRequest request, HttpServletResponse response)
        throws IOException, InterruptedException, ServletException {

        File file = ZipSimulationUtil.getSimulationZip(simulation.getSimulationDirectory());
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        
        FilePath filePath = new FilePath(file);
        filePath.copyTo(response.getOutputStream());
    }
}