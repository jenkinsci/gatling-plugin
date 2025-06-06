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

import hudson.model.Action;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/** This class is used to download the zipped Reports file */
public class ReportDownloader implements Action {

  private final BuildSimulation simulation;

  public ReportDownloader(BuildSimulation simulation) {
    this.simulation = simulation;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Download Gatling Report";
  }

  @Override
  public String getUrlName() {
    return "download";
  }

  public void doDynamic(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException {
    File reportDir = simulation.getSimulationDirectory();
    if (!reportDir.exists() || !reportDir.isDirectory()) {
      rsp.sendError(404, "Report not found");
      return;
    }

    // Set headers for zip download
    rsp.setContentType("application/zip");
    rsp.setHeader("Content-Disposition", "attachment; filename=" + reportDir.getName() + ".zip");

    // Create zip file
    try (ZipOutputStream zos = new ZipOutputStream(rsp.getOutputStream())) {
      addToZip(reportDir, reportDir.getName(), zos);
    }
  }

  private void addToZip(File file, String entryName, ZipOutputStream zos) throws IOException {
    if (file.isDirectory()) {
      // Add directory entry
      zos.putNextEntry(new ZipEntry(entryName + "/"));
      zos.closeEntry();

      // Add all files in directory
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) {
          addToZip(f, entryName + "/" + f.getName(), zos);
        }
      }
    } else {
      // Add file entry
      zos.putNextEntry(new ZipEntry(entryName));
      Files.copy(file.toPath(), zos);
      zos.closeEntry();
    }
  }
}
