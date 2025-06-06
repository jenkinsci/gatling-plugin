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
package io.gatling.jenkins.steps;

import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.slaves.DumbSlave;
import hudson.tasks.Shell;
import io.gatling.jenkins.GatlingBuildAction;
import io.gatling.jenkins.GatlingPublisher;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

public class GatlingArchiverStepTest extends Assert {
  @Rule public JenkinsRule j = new JenkinsRule();

  /** Test archiving of gatling reports */
  @Test
  public void archive() throws Exception {
    // job setup
    WorkflowJob foo = j.jenkins.createProject(WorkflowJob.class, "foo");
    foo.setDefinition(
        new CpsFlowDefinition(
            StringUtils.join(
                Arrays.asList(
                    "node {",
                    "  sleep 1", // JENKINS-51015
                    "  writeFile file: 'results/foo-1234/index.html', text: '<html><body><div id=\"container_statistics_body\"><table><tbody><tr class=\"total col-1\"><td>Total</td><td class=\"value total col-2\">100</td><td class=\"value ok col-3\">95</td><td class=\"value ko col-4\">5</td><td class=\"value total col-7\">50</td></tr></tbody></table></div></body></html>'",
                    "  writeFile file: 'results/foo-4321/index.html', text: '<html><body><div id=\"container_statistics_body\"><table><tbody><tr class=\"total col-1\"><td>Total</td><td class=\"value total col-2\">100</td><td class=\"value ok col-3\">95</td><td class=\"value ko col-4\">5</td><td class=\"value total col-7\">50</td></tr></tbody></table></div></body></html>'",
                    "  writeFile file: 'results/bar-5678/index.html', text: '<html><body><div id=\"container_statistics_body\"><table><tbody><tr class=\"total col-1\"><td>Total</td><td class=\"value total col-2\">100</td><td class=\"value ok col-3\">95</td><td class=\"value ko col-4\">5</td><td class=\"value total col-7\">50</td></tr></tbody></table></div></body></html>'",
                    "  gatlingArchive()",
                    "}"),
                "\n")));

    // get the build going, and wait until workflow pauses
    WorkflowRun b = j.assertBuildStatusSuccess(foo.scheduleBuild2(0).get());
    verifyResult(b);
  }

  @Test
  @Issue("JENKINS-50977")
  public void archiveInFreestyle() throws Exception {
    Assume.assumeFalse("The test is Unix-only", hudson.remoting.Launcher.isWindows());
    FreeStyleProject foo = j.createFreeStyleProject();
    DumbSlave onlineSlave = j.createOnlineSlave();

    foo.setAssignedNode(onlineSlave);
    foo.getBuildersList()
        .add(
            new Shell(
                "\n"
                    + "sleep 1 \n"
                    + // Otherwise GatlingPublisher skips that because BuildStart time has
                    // second-accuracy (JENKINS-51015)
                    "mkdir -p results/foo-1234/\n"
                    + "echo '<html><body><div id=\"container_statistics_body\"><table><tbody><tr class=\"total col-1\"><td>Total</td><td class=\"value total col-2\">100</td><td class=\"value ok col-3\">95</td><td class=\"value ko col-4\">5</td><td class=\"value total col-7\">50</td></tr></tbody></table></div></body></html>' > results/foo-1234/index.html\n"
                    + "mkdir -p results/foo-4321/\n"
                    + "echo '<html><body><div id=\"container_statistics_body\"><table><tbody><tr class=\"total col-1\"><td>Total</td><td class=\"value total col-2\">100</td><td class=\"value ok col-3\">95</td><td class=\"value ko col-4\">5</td><td class=\"value total col-7\">50</td></tr></tbody></table></div></body></html>' > results/foo-4321/index.html\n"
                    + "mkdir -p results/bar-5678/\n"
                    + "echo '<html><body><div id=\"container_statistics_body\"><table><tbody><tr class=\"total col-1\"><td>Total</td><td class=\"value total col-2\">100</td><td class=\"value ok col-3\">95</td><td class=\"value ko col-4\">5</td><td class=\"value total col-7\">50</td></tr></tbody></table></div></body></html>' > results/bar-5678/index.html\n"));
    foo.getPublishersList().add(new GatlingPublisher(true));

    // Build and assert status
    FreeStyleBuild freeStyleBuild = j.buildAndAssertSuccess(foo);
    verifyResult(freeStyleBuild);

    // Triggers JEP-200 if PrintStream is still persisted
    foo.save();
  }

  private void verifyResult(Run b) throws Exception {
    File baseDir = b.getRootDir();
    File fooArchiveDir = new File(baseDir, "simulations/foo-1234");
    assertTrue("foo archive dir doesn't exist: " + fooArchiveDir, fooArchiveDir.isDirectory());

    File foo2ArchiveDir = new File(baseDir, "simulations/foo-4321");
    assertTrue("foo archive dir doesn't exist: " + foo2ArchiveDir, foo2ArchiveDir.isDirectory());

    File barArchiveDir = new File(baseDir, "simulations/bar-5678");
    assertTrue("bar archive dir doesn't exist: " + barArchiveDir, barArchiveDir.isDirectory());

    List<GatlingBuildAction> gbas = new ArrayList<>();
    for (Action a : b.getAllActions()) {
      if (a instanceof GatlingBuildAction) {
        gbas.add((GatlingBuildAction) a);
      }
    }
    assertEquals("Should be exactly one GatlingBuildAction", 1, gbas.size());

    GatlingBuildAction buildAction = gbas.get(0);
    assertEquals(
        "BuildAction should have exactly one ProjectAction",
        1,
        buildAction.getProjectActions().size());

    // TODO JENKINS-57244 use RestartableJenkinsRule to verify that the action was actually saved
    // successfully
    FileUtils.copyFile(new File(b.getRootDir(), "build.xml"), System.out);
  }
}
