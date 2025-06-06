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

import hudson.FilePath;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ZipSimulationUtil {
  private static final Logger LOGGER = Logger.getLogger(ZipSimulationUtil.class.getName());

  private static File zipSimulation(FilePath simulationFilePath) throws InterruptedException {
    File zippedFile = new File(simulationFilePath + ".zip");
    try {
      simulationFilePath.zip(new FilePath(zippedFile));
    } catch (IOException e) {
      LOGGER.log(Level.INFO, e.getMessage(), e);
    }
    return zippedFile;
  }

  public static File getSimulationZip(File simulationDirectory) throws InterruptedException {
    File simulationFile = new File(simulationDirectory.getAbsolutePath() + ".zip");
    if (!simulationFile.exists()) {
      return zipSimulation(new FilePath(simulationDirectory));
    }
    return simulationFile;
  }
}
