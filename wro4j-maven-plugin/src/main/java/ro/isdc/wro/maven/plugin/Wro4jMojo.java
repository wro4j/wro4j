/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.mockito.Mockito;

import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * @goal run
 * @phase process-resources
 *
 * @author Alex Objelean
 */
public class Wro4jMojo
  extends AbstractMojo {
  /**
   * File containing the groups definitions.
   *
   * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/wro.xml
   */
  private File wroFile;
  /**
   * The folder where web application context resides useful for locating resources relative to servletContext .
   *
   * @parameter default-value="${basedir}/src/main/webapp/
   */
  private File contextFolder;
  /**
   * The path to the destination directory where the files are stored at the end of the process.
   *
   * @parameter default-value="${project.build.directory}/wro/"
   */
  private File destinationFolder;
  /**
   * Comma separated group names.
   *
   * @parameter expression="${targetGroups}"
   */
  private String targetGroups;
  /**
   * @parameter default-value="true" expression="${minimize}"
   * @optional
   */
  private boolean minimize;
  /**
   * @parameter expression="${wroManagerFactory}"
   * @optional
   */
  private String wroManagerFactory;

  /**
   * @parameter default-value="${project}"
   */
  private MavenProject mavenProject;


  /**
   * @param request {@link HttpServletRequest} to process
   * @return {@link WroManagerFactory} implementation.
   */
  @SuppressWarnings("unchecked")
  private MavenContextAwareManagerFactory getManagerFactory(final HttpServletRequest request)
    throws MojoExecutionException {
    final MavenContextAwareManagerFactory managerFactory;
    if (wroManagerFactory != null) {
      try {
        final Class<? extends MavenContextAwareManagerFactory> wroManagerFactoryClass = (Class<? extends MavenContextAwareManagerFactory>)Thread.currentThread().getContextClassLoader().loadClass(wroManagerFactory);
        managerFactory = wroManagerFactoryClass.newInstance();
      } catch (final Exception e) {
        throw new MojoExecutionException("Invalid wroManagerFactory className: " + wroManagerFactory);
      }
    } else {
      managerFactory = new DefaultMavenContextAwareManagerFactory();
    }
    // initialize before return.
    managerFactory.initialize(createRunContext(), request);
    return managerFactory;
  }


  /**
   * Creates a {@link RunContext} by setting properties passed after mojo is initialized.
   */
  private RunContext createRunContext() {
    final RunContext runContext = new RunContext();
    runContext.setContextFolder(contextFolder);
    runContext.setMinimize(minimize);
    runContext.setWroFile(wroFile);
    return runContext;
  }


  /**
   * {@inheritDoc}
   */
  public void execute()
    throws MojoExecutionException {
    validate();
    updateClasspath();
    getLog().info("Executing the mojo: ");
    getLog().info("Wro4j Model path: " + wroFile.getPath());
    getLog().info("TargetGroups: " + targetGroups);
    getLog().info("Minimize: " + minimize);
    getLog().info("Destination folder: " + destinationFolder);

    // updateClasspath();
    try {
      if (!destinationFolder.exists()) {
        destinationFolder.mkdirs();
      }
      getLog().info("will process the following groups: " + targetGroups);
      // TODO create a Request object
      for (final String group : getTargetGroupsAsList()) {
        for (final ResourceType resourceType : ResourceType.values()) {
          final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
          processGroup(groupWithExtension);
        }
      }
    } catch (final Exception e) {
      throw new MojoExecutionException("Exception occured while processing: " + e.getMessage(), e);
    }
  }


  /**
   * Checks if all required fields are configured.
   */
  private void validate()
    throws MojoExecutionException {
    if (wroFile == null) {
      throw new MojoExecutionException("wroFile was not set!");
    }
    if (destinationFolder == null) {
      throw new MojoExecutionException("destinationFolder was not set!");
    }
    if (targetGroups == null) {
      throw new MojoExecutionException("targetGroups was not set!");
    }
    if (contextFolder == null) {
      throw new MojoExecutionException("contextFolder was not set!");
    }
  }


  /**
   * Update the classpath.
   */
  private void updateClasspath() {
    // TODO update classloader by adding all runtime dependencies of the running project
    getLog().info("mavenProject: " + mavenProject);
    final Collection<Artifact> artifacts = mavenProject.getArtifacts();
    final List<URL> urlList = new ArrayList<URL>();
    try {
      for (final Artifact artifact : artifacts) {
        urlList.add(artifact.getFile().toURI().toURL());
      }
    } catch (final MalformedURLException e) {
      getLog().error("Error retreiving URL for artifact", e);
      throw new RuntimeException(e);
    }
    getLog().info("URLs: " + urlList);
    final URLClassLoader cl = new URLClassLoader(urlList.toArray(new URL[] {}), Thread.currentThread().getContextClassLoader());
    Thread.currentThread().setContextClassLoader(cl);
  }


  /**
   * @return a list containing all groups needs to be processed.
   */
  private List<String> getTargetGroupsAsList() {
    return Arrays.asList(targetGroups.split(","));
  }


  /**
   * Process a single group.
   *
   * @throws IOException if any IO related exception occurs.
   */
  private void processGroup(final String group)
    throws IOException, MojoExecutionException {
    getLog().info("processing group: " + group);

    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(request.getRequestURI()).thenReturn(group);

    final File destinationFile = new File(destinationFolder, group);
    destinationFile.createNewFile();
    final FileOutputStream fos = new FileOutputStream(destinationFile);
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(fos));

    getManagerFactory(request).getInstance().process(request, response);

    fos.close();
    // delete empty files
    if (destinationFile.length() == 0) {
      getLog().info("No content found for group: " + group);
      destinationFile.delete();
    } else {
      getLog().info(destinationFile.getAbsolutePath() + " (" + destinationFile.length() + "bytes"
        + ") has been created!");
    }
  }


  /**
   * @param wroFile the wroFile to set
   */
  public void setWroFile(final File wroFile) {
    this.wroFile = wroFile;
  }


  /**
   * @param destinationFolder the destinationFolder to set
   */
  public void setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
  }


  /**
   * @param contextFolder the servletContextFolder to set
   */
  public void setContextFolder(final File contextFolder) {
    this.contextFolder = contextFolder;
  }


  /**
   * @param targetGroups comma separated group names.
   */
  public void setTargetGroups(final String targetGroups) {
    this.targetGroups = targetGroups;
  }


  /**
   * @param minimize flag for minimization.
   */
  public void setMinimize(final boolean minimize) {
    this.minimize = minimize;
  }


  /**
   * @param wroManagerFactory the wroManagerFactory to set
   */
  public void setWroManagerFactory(final String wroManagerFactory) {
    this.wroManagerFactory = wroManagerFactory;
  }
}
