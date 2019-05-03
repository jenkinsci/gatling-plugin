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
import java.io.File;

/**
 * This class is basically just a struct to hold information about one
 * or more gatling simulations that were archived for a given
 * instance of {@link GatlingBuildAction}.
 */
public class BuildSimulation {
  private final String simulationName;
  private final RequestReport requestReport;
  @Deprecated
  private transient FilePath simulationDirectory;
  // TODO better to save, for example, a relative path from Run.rootDir
  private File simulationPath;

  public BuildSimulation(String simulationName, RequestReport requestReport, File simulationPath) {
    this.simulationName = simulationName;
    this.requestReport = requestReport;
    this.simulationPath = simulationPath;
  }

  private Object readResolve() {
    if (simulationDirectory != null) {
        simulationPath = new File(simulationDirectory.getRemote());
    }
    return this;
  }

  public String getSimulationName() {
    return simulationName;
  }

  public RequestReport getRequestReport() {
    return requestReport;
  }

  public File getSimulationDirectory() {
    return simulationPath;
  }
}
