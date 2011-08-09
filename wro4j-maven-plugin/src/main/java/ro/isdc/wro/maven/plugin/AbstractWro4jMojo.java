/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.manager.standalone.ExtensionsStandaloneManagerFactory;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;


/**
 * Defines most common properties used by wro4j build-time solution infrastructure.
 *
 * @author Alex Objelean
 */
public abstract class AbstractWro4jMojo extends AbstractMojo {
  /**
   * File containing the groups definitions.
   *
   * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/wro.xml"
   * @optional
   */
  private File wroFile;
  /**
   * The folder where web application context resides useful for locating resources relative to servletContext .
   *
   * @parameter default-value="${basedir}/src/main/webapp/"
   */
  private File contextFolder;
  /**
   * @parameter default-value="true" expression="${minimize}"
   * @optional
   */
  private boolean minimize;
  /**
   * @parameter default-value="true" expression="${ignoreMissingResources}"
   * @optional
   */
  private boolean ignoreMissingResources;
  /**
   * Comma separated group names. This field is optional. If no value is provided, a file for each group will be
   * created.
   *
   * @parameter expression="${targetGroups}"
   * @optional
   */
  private String targetGroups;
  /**
   * @parameter default-value="${project}"
   */
  private MavenProject mavenProject;
  /**
   * An instance of {@link StandaloneContextAwareManagerFactory}.
   */
  private StandaloneContextAwareManagerFactory managerFactory;


  /**
   * {@inheritDoc}
   */
  public final void execute()
    throws MojoExecutionException {
    validate();
    extendPluginClasspath();
    getLog().info("Executing the mojo: ");
    getLog().info("Wro4j Model path: " + wroFile.getPath());
    getLog().info("targetGroups: " + getTargetGroups());
    getLog().info("minimize: " + isMinimize());
    getLog().info("ignoreMissingResources: " + isIgnoreMissingResources());

    Context.set(Context.standaloneContext());
    try {
      doExecute();
    } catch (final Exception e) {
      throw new MojoExecutionException("Exception occured while processing: " + e.getMessage(), e);
    }
  }


  /**
   * Creates a {@link StandaloneContext} by setting properties passed after mojo is initialized.
   */
  protected final StandaloneContext createStandaloneContext() {
    final StandaloneContext runContext = new StandaloneContext();
    runContext.setContextFolder(getContextFolder());
    runContext.setMinimize(isMinimize());
    runContext.setWroFile(getWroFile());
    runContext.setIgnoreMissingResources(isIgnoreMissingResources());
    return runContext;
  }


  /**
   * Perform actual plugin processing.
   */
  protected abstract void doExecute() throws Exception;

  /**
   * This method will ensure that you have a right and initialized instance of
   * {@link StandaloneContextAwareManagerFactory}.
   *
   * @return {@link WroManagerFactory} implementation.
   */
  protected final StandaloneContextAwareManagerFactory getManagerFactory()
    throws Exception {
    if (managerFactory == null) {
      managerFactory = newWroManagerFactory();
      // initialize before process.
      managerFactory.initialize(createStandaloneContext());
    }
    return managerFactory;
  }

  protected StandaloneContextAwareManagerFactory newWroManagerFactory() throws MojoExecutionException {
    return new ExtensionsStandaloneManagerFactory();
  }

  /**
   * @return a list containing all groups needs to be processed.
   */
  protected final List<String> getTargetGroupsAsList()
    throws Exception {
    if (getTargetGroups() == null) {
      final WroModel model = getManagerFactory().create().getModel();
      return model.getGroupNames();
    }
    return Arrays.asList(getTargetGroups().split(","));
  }

  /**
   * Checks if all required fields are configured.
   */
  protected void validate()
    throws MojoExecutionException {
    if (wroFile == null) {
      throw new MojoExecutionException("contextFolder was not set!");
    }
    if (contextFolder == null) {
      throw new MojoExecutionException("contextFolder was not set!");
    }
  }

  /**
   * Update the classpath.
   */
  @SuppressWarnings("unchecked")
  protected final void extendPluginClasspath()
    throws MojoExecutionException {
    // this code is inspired from http://teleal.org/weblog/Extending%20the%20Maven%20plugin%20classpath.html
    final List<String> classpathElements = new ArrayList<String>();
    try {
      classpathElements.addAll(mavenProject.getRuntimeClasspathElements());
    } catch (final DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Could not get compile classpath elements", e);
    }
    final ClassLoader classLoader = createClassLoader(classpathElements);
    Thread.currentThread().setContextClassLoader(classLoader);
  }


  /**
   * @return {@link ClassRealm} based on project dependencies.
   */
  private ClassLoader createClassLoader(final List<String> classpathElements) {
    getLog().debug("Classpath elements:");
    final List<URL> urls = new ArrayList<URL>();
    try {
      for (final String element : classpathElements) {
        final File elementFile = new File(element);
        getLog().debug("Adding element to plugin classpath: " + elementFile.getPath());
        urls.add(elementFile.toURI().toURL());
      }
    } catch (final Exception e) {
      getLog().error("Error retreiving URL for artifact", e);
      throw new RuntimeException(e);
    }
    return new URLClassLoader(urls.toArray(new URL[] {}), Thread.currentThread().getContextClassLoader());
  }

  /**
   * @param contextFolder the servletContextFolder to set
   */
  public void setContextFolder(final File contextFolder) {
    this.contextFolder = contextFolder;
  }

  /**
   * @param wroFile the wroFile to set
   */
  public void setWroFile(final File wroFile) {
    this.wroFile = wroFile;
  }

  /**
   * @return the wroFile
   */
  public File getWroFile() {
    return this.wroFile;
  }

  /**
   * @return the contextFolder
   */
  public File getContextFolder() {
    return this.contextFolder;
  }

  /**
   * @param minimize flag for minimization.
   */
  public void setMinimize(final boolean minimize) {
    this.minimize = minimize;
  }

  /**
   * @param ignoreMissingResources the ignoreMissingResources to set
   */
  public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    this.ignoreMissingResources = ignoreMissingResources;
  }


  /**
   * @return the minimize
   */
  public boolean isMinimize() {
    return this.minimize;
  }

  /**
   * @return the ignoreMissingResources
   */
  public boolean isIgnoreMissingResources() {
    return this.ignoreMissingResources;
  }


  /**
   * Used for testing.
   *
   * @param mavenProject the mavenProject to set
   */
  void setMavenProject(final MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  /**
   * @return the targetGroups
   */
  public String getTargetGroups() {
    return this.targetGroups;
  }

  /**
   * @param versionEncoder(targetGroups) comma separated group names.
   */
  public void setTargetGroups(final String targetGroups) {
    this.targetGroups = targetGroups;
  }
}
