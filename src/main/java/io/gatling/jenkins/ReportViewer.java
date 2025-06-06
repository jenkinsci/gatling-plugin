package io.gatling.jenkins;

import hudson.model.Action;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ReportViewer implements Action {
  private final BuildSimulation simulation;

  public ReportViewer(BuildSimulation simulation) {
    this.simulation = simulation;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Gatling Report";
  }

  @Override
  public String getUrlName() {
    return "report";
  }

  public void doIndex(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException {
    File reportDir = simulation.getSimulationDirectory();
    File indexFile = new File(reportDir, "index.html");

    if (!indexFile.exists()) {
      rsp.sendError(404, "Report not found");
      return;
    }

    // Read the HTML content
    String htmlContent = new String(Files.readAllBytes(indexFile.toPath()), StandardCharsets.UTF_8);

    // Remove any script tags and their contents
    htmlContent = htmlContent.replaceAll("<script[^>]*>.*?</script>", "");
    // Remove any event handlers
    htmlContent = htmlContent.replaceAll("\\s+on\\w+\\s*=\\s*\"[^\"]*\"", "");
    htmlContent = htmlContent.replaceAll("\\s+on\\w+\\s*=\\s*'[^']*'", "");
    // Remove any javascript: URLs
    htmlContent = htmlContent.replaceAll("javascript:[^\"']*", "");

    // Set security headers
    rsp.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");
    rsp.setHeader("X-Content-Type-Options", "nosniff");
    rsp.setHeader("X-Frame-Options", "SAMEORIGIN");
    rsp.setContentType("text/html;charset=UTF-8");

    try (OutputStream out = rsp.getOutputStream()) {
      out.write(htmlContent.getBytes(StandardCharsets.UTF_8));
    }
  }

  public void doDynamic(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException {
    String path = req.getRestOfPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    File reportDir = simulation.getSimulationDirectory();
    File requestedFile = new File(reportDir, path);

    if (!requestedFile.exists()
        || !requestedFile.getCanonicalPath().startsWith(reportDir.getCanonicalPath())) {
      rsp.sendError(404, "File not found");
      return;
    }

    // Set security headers
    rsp.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");
    rsp.setHeader("X-Content-Type-Options", "nosniff");
    rsp.setHeader("X-Frame-Options", "SAMEORIGIN");

    String contentType = getContentType(path);
    rsp.setContentType(contentType);

    if (path.endsWith(".html")) {
      // Read and sanitize HTML content
      String htmlContent =
          new String(
              java.nio.file.Files.readAllBytes(requestedFile.toPath()), StandardCharsets.UTF_8);

      // Remove any script tags and their contents
      htmlContent = htmlContent.replaceAll("<script[^>]*>.*?</script>", "");
      // Remove any event handlers
      htmlContent = htmlContent.replaceAll("\\s+on\\w+\\s*=\\s*\"[^\"]*\"", "");
      htmlContent = htmlContent.replaceAll("\\s+on\\w+\\s*=\\s*'[^']*'", "");
      // Remove any javascript: URLs
      htmlContent = htmlContent.replaceAll("javascript:[^\"']*", "");
      // Remove any data: URLs except for images
      htmlContent = htmlContent.replaceAll("data:(?!image/)[^\"']*", "");

      try (OutputStream out = rsp.getOutputStream()) {
        out.write(htmlContent.getBytes(StandardCharsets.UTF_8));
      }
    } else {
      // Serve non-HTML files as-is
      try (InputStream in = new FileInputStream(requestedFile);
          OutputStream out = rsp.getOutputStream()) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }

  private String getContentType(String path) {
    if (path.endsWith(".html")) return "text/html;charset=UTF-8";
    if (path.endsWith(".css")) return "text/css;charset=UTF-8";
    if (path.endsWith(".js")) return "application/javascript;charset=UTF-8";
    if (path.endsWith(".png")) return "image/png";
    if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
    if (path.endsWith(".gif")) return "image/gif";
    if (path.endsWith(".svg")) return "image/svg+xml";
    return "application/octet-stream";
  }
}
