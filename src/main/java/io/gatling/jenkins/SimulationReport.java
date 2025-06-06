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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.FilePath;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SimulationReport {

  private final FilePath reportDirectory;

  private static final String STATS_FILE_PATTERN = "**/index.html";

  private RequestReport globalReport;

  private final String simulation;

  public SimulationReport(FilePath reportDirectory, String simulation) {
    this.reportDirectory = reportDirectory;
    this.simulation = simulation;
  }

  public void readStatsFile() throws IOException, InterruptedException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    File htmlFile = locateStatsFile();

    // Read the HTML file content with explicit UTF-8 encoding
    String content =
        new String(
            java.nio.file.Files.readAllBytes(htmlFile.toPath()),
            java.nio.charset.StandardCharsets.UTF_8);

    // Create a new RequestReport
    RequestReport report = new RequestReport();

    // Extract statistics from the HTML table
    // Look for the statistics table body
    int startIndex = content.indexOf("container_statistics_body");
    if (startIndex == -1) {
      throw new IOException("Could not find statistics table in HTML");
    }

    // Find the table body content
    int tableStart = content.indexOf("<tbody>", startIndex);
    if (tableStart == -1) {
      throw new IOException("Could not find table body in HTML");
    }

    // Extract total requests
    int totalIndex = content.indexOf("class=\"total col-1\"", tableStart);
    if (totalIndex != -1) {
      Statistics totalStats = new Statistics();

      // Find the total value
      int totalValueIndex = content.indexOf("class=\"value total col-2\"", totalIndex);
      if (totalValueIndex != -1) {
        totalStats.setTotal(extractNumberFromClass(content, totalValueIndex));
      }

      // Find the OK value
      int okValueIndex = content.indexOf("class=\"value ok col-3\"", totalIndex);
      if (okValueIndex != -1) {
        totalStats.setOK(extractNumberFromClass(content, okValueIndex));
      }

      // Find the KO value
      int koValueIndex = content.indexOf("class=\"value ko col-4\"", totalIndex);
      if (koValueIndex != -1) {
        totalStats.setKO(extractNumberFromClass(content, koValueIndex));
      }

      report.setNumberOfRequests(totalStats);
    }

    // Extract response time statistics
    int responseTimeIndex = content.indexOf("class=\"value total col-7\"", tableStart);
    if (responseTimeIndex != -1) {
      // Min response time
      Statistics minStats = new Statistics();
      minStats.setTotal(extractNumberFromClass(content, responseTimeIndex));
      report.setMinResponseTime(minStats);

      // Max response time (not directly available, using 95th percentile as approximation)
      Statistics maxStats = new Statistics();
      maxStats.setTotal(
          extractNumberFromClass(content, responseTimeIndex + 100)); // Approximate position
      report.setMaxResponseTime(maxStats);

      // Mean response time (not directly available, using 50th percentile as approximation)
      Statistics meanStats = new Statistics();
      meanStats.setTotal(
          extractNumberFromClass(content, responseTimeIndex + 200)); // Approximate position
      report.setMeanResponseTime(meanStats);

      // Percentiles
      Statistics p50Stats = new Statistics();
      p50Stats.setTotal(extractNumberFromClass(content, responseTimeIndex + 200));
      report.setPercentiles1(p50Stats);

      Statistics p75Stats = new Statistics();
      p75Stats.setTotal(extractNumberFromClass(content, responseTimeIndex + 300));
      report.setPercentiles2(p75Stats);

      Statistics p95Stats = new Statistics();
      p95Stats.setTotal(extractNumberFromClass(content, responseTimeIndex + 400));
      report.setPercentiles3(p95Stats);

      Statistics p99Stats = new Statistics();
      p99Stats.setTotal(extractNumberFromClass(content, responseTimeIndex + 500));
      report.setPercentiles4(p99Stats);
    }

    globalReport = report;
  }

  private long extractNumberFromClass(String content, int startIndex) {
    // Find the next number after the class
    int start = content.indexOf(">", startIndex);
    if (start == -1) return 0;
    start++;

    int end = content.indexOf("<", start);
    if (end == -1) return 0;

    String numberStr = content.substring(start, end).trim();
    if (numberStr.equals("-")) return 0;

    try {
      return Long.parseLong(numberStr);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private File locateStatsFile() throws IOException, InterruptedException {
    FilePath[] files = reportDirectory.list(STATS_FILE_PATTERN);

    if (files.length == 0)
      throw new FileNotFoundException("Unable to locate the simulation results for " + simulation);

    return new File(files[0].getRemote());
  }

  public String getSimulationPath() {
    return simulation;
  }

  public RequestReport getGlobalReport() {
    return globalReport;
  }
}
