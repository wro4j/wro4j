/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import static org.apache.commons.lang3.Validate.notNull;

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
import org.sonatype.plexus.build.incremental.BuildContext;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.manager.standalone.ExtensionsStandaloneManagerFactory;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAware;
import ro.isdc.wro.maven.plugin.support.ExtraConfigFileAware;
import ro.isdc.wro.maven.plugin.support.ResourceChangeHandler;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.concurrent.TaskExecutor;

import com.google.common.annotations.VisibleForTesting;


/**
 * Defines most common properties used by wro4j build-time solution infrastructure.
 *
 * @author Alex Objelean
 */
public abstract class AbstractWro4jMojo
    extends AbstractMojo {
  /**
   * File containing the groups definitions.
   *
   * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/wro.xml" expression="${wroFile}"
   * @optional
   */
  private File wroFile;
  /**
   * Whether to skip executing Wro4j. Allows clients to pass a build-time parameter to skip running wro4j.
   *
   * @parameter default-value=false
   * @optional
   */
   private boolean skip;
  /**
   * The folder where web application context resides useful for locating resources relative to servletContext. It is
   * possible to provide multiple context folders using a CSV. When multiple contextFolders are provided, the
   * servletContext locator will try to search in next contextFolder when a resource could not be located. By default, a
   * single context folder is configured.
   *
   * @parameter default-value="${basedir}/src/main/webapp/" expression="${contextFolder}"
   * @optional
   */
  private String contextFolder;
  /**
   * @parameter default-value="true" expression="${minimize}"
   * @optional
   */
  private boolean minimize;
  /**
   * @parameter expression="${ignoreMissingResources}"
   * @optional
   */
  private String ignoreMissingResources;
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
   * @parameter expression="${wroManagerFactory}"
   * @optional
   */
  private String wroManagerFactory;
  /**
   * An instance of {@link StandaloneContextAware}.
   */
  private WroManagerFactory managerFactory;
  /**
   * The path to configuration file.
   *
   * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/wro.properties" expression="${extraConfigFile}"
   * @optional
   */
  private File extraConfigFile;
  /**
   * Responsible for identifying the resources changed during incremental build.
   * <p/>
   * Read more about it <a href="http://wiki.eclipse.org/M2E_compatible_maven_plugins#BuildContext">here</a>
   *
   * @component
   */
  private BuildContext buildContext;
  /**
   * This parameter is not meant to be used. The only purpose is to hold project build directory
   *
   * @parameter default-value="${project.build.directory}"
   * @optional
   */
  private File buildDirectory;
  /**
   * When this flag is enabled and there are more than one group to be processed, these will be processed in parallel,
   * resulting in faster overall plugin execution time.
   *
   * @parameter default-value="false" expression="${parallelProcessing}"
   * @optional
   */
  private boolean parallelProcessing;
  /**
   * Flag which allows to enable incremental build (experimental feature). It is false by default, but probably can be
   * changed to true if no unexpected problems are detected..
   *
   * @parameter default-value="false" expression="${incrementalBuildEnabled}"
   * @optional
   */
  private boolean incrementalBuildEnabled;
  private TaskExecutor<Void> taskExecutor;
  private ResourceChangeHandler resourceChangeHandler;

  /**
   * {@inheritDoc}
   */
  public final void execute()
      throws MojoExecutionException {
    validate();
    if (skip) {
      getLog().info("Skipping Wro4j execution");
      return;
    }
    getLog().info(contextFolder);
    getLog().info("Executing the mojo: ");
    getLog().info("Wro4j Model path: " + wroFile.getPath());
    getLog().info("targetGroups: " + getTargetGroups());
    getLog().info("minimize: " + isMinimize());
    getLog().info("ignoreMissingResources: " + isIgnoreMissingResources());
    getLog().info("parallelProcessing: " + isParallelProcessing());
    getLog().debug("wroManagerFactory: " + wroManagerFactory);
    getLog().debug("incrementalBuildEnabled: " + incrementalBuildEnabled);
    getLog().debug("extraConfig: " + extraConfigFile);

    extendPluginClasspath();
    Context.set(Context.standaloneContext());
    try {
      onBeforeExecute();
      doExecute();
    } catch (final Exception e) {
      final String message = "Exception occured while processing: " + e.toString() + ", class: "
          + e.getClass().getName() + ",caused by: " + (e.getCause() != null ? e.getCause().getClass().getName() : "");
      getLog().error(message, e);
      if (e instanceof WroRuntimeException) {
        // Do not keep resources which cause the exception. This is helpful for linter processors.
        final Resource resource = ((WroRuntimeException) e).getResource();
        forgetResource(resource);
      }
      throw new MojoExecutionException(message, e);
    } finally {
      try {
        onAfterExecute();
      } catch (final Exception e) {
        throw new MojoExecutionException("Exception in onAfterExecute", e);
      }
    }
  }

  /**
   * Safely invoke {@link ResourceChangeHandler#forget(Resource)}. The safety is required because invoking
   * {@link #getResourceChangeHandler()} can throw an exception during initialization.
   */
  private void forgetResource(final Resource resource) {
    if (resourceChangeHandler != null) {
      resourceChangeHandler.forget(resource);
    }
  }

  /**
   * Creates a {@link StandaloneContext} by setting properties passed after mojo is initialized.
   */
  private StandaloneContext createStandaloneContext() {
    final StandaloneContext runContext = new StandaloneContext();
    runContext.setContextFoldersAsCSV(getContextFoldersAsCSV());
    runContext.setMinimize(isMinimize());
    runContext.setWroFile(getWroFile());
    runContext.setIgnoreMissingResourcesAsString(isIgnoreMissingResources());
    return runContext;
  }

  /**
   * Perform actual plugin processing.
   */
  protected abstract void doExecute()
      throws Exception;

  /**
   * This method will ensure that you have a right and initialized instance of {@link StandaloneContextAware}. When
   * overriding this method, ensure that creating managerFactory performs injection during manager creation, otherwise
   * the manager won't be initialized properly.
   *
   * @return {@link WroManagerFactory} implementation.
   */
  protected WroManagerFactory getManagerFactory() {
    if (managerFactory == null) {
      WroManagerFactory localManagerFactory = null;
      try {
        localManagerFactory = newWroManagerFactory();
      } catch (final MojoExecutionException e) {
        throw WroRuntimeException.wrap(e);
      }
      // initialize before process.
      if (localManagerFactory instanceof StandaloneContextAware) {
        ((StandaloneContextAware) localManagerFactory).initialize(createStandaloneContext());
      }
      managerFactory = decorateManagerFactory(localManagerFactory);
    }
    return managerFactory;
  }

  /**
   * Allows the initialized manager factory to be decorated.
   */
  protected WroManagerFactory decorateManagerFactory(final WroManagerFactory managerFactory) {
    return managerFactory;
  }

  /**
   * {@inheritDoc}
   */
  protected WroManagerFactory newWroManagerFactory()
      throws MojoExecutionException {
    WroManagerFactory factory = null;
    if (wroManagerFactory != null) {
      factory = createCustomManagerFactory();
    } else {
      factory = new ExtensionsStandaloneManagerFactory();
    }
    getLog().info("wroManagerFactory class: " + factory.getClass().getName());

    if (factory instanceof ExtraConfigFileAware) {
      if (extraConfigFile == null) {
        throw new MojoExecutionException("The " + factory.getClass() + " requires a valid extraConfigFile!");
      }
      getLog().debug("Using extraConfigFile: " + extraConfigFile.getAbsolutePath());
      ((ExtraConfigFileAware) factory).setExtraConfigFile(extraConfigFile);
    }
    return factory;
  }

  /**
   * Creates an instance of Manager factory based on the value of the wroManagerFactory plugin parameter value.
   */
  private WroManagerFactory createCustomManagerFactory()
      throws MojoExecutionException {
    WroManagerFactory managerFactory;
    try {
      final Class<?> wroManagerFactoryClass = Thread.currentThread().getContextClassLoader().loadClass(
          wroManagerFactory.trim());
      managerFactory = (WroManagerFactory) wroManagerFactoryClass.newInstance();
    } catch (final Exception e) {
      throw new MojoExecutionException("Invalid wroManagerFactoryClass, called: " + wroManagerFactory, e);
    }
    return managerFactory;
  }

  /**
   * @return a list of groups which will be processed.
   */
  protected final List<String> getTargetGroupsAsList()
      throws Exception {
    List<String> result = null;
    if (isIncrementalCheckRequired()) {
      result = getIncrementalGroupNames();
    } else if (getTargetGroups() == null) {
      result = getAllModelGroupNames();
    } else {
      result = Arrays.asList(getTargetGroups().split(","));
    }
    persistResourceFingerprints(result);
    if (result.isEmpty()) {
      getLog().info("Nothing to process (nothing configured or nothing changed since last build).");
    } else {
      getLog().info("The following groups will be processed: " + result);
    }
    return result;
  }

  /**
   * Store digest for all resources contained inside the list of provided groups.
   */
  private void persistResourceFingerprints(final List<String> groupNames) {
    final WroModelInspector modelInspector = new WroModelInspector(getModel());
    for (final String groupName : groupNames) {
      final Group group = modelInspector.getGroupByName(groupName);
      if (group != null) {
        for (final Resource resource : group.getResources()) {
          getResourceChangeHandler().remember(resource);
        }
      }
    }
  }

  /**
   * @return a list of groups changed by incremental builds.
   */
  private List<String> getIncrementalGroupNames()
      throws Exception {
    final List<String> changedGroupNames = new ArrayList<String>();
    for (final Group group : getModel().getGroups()) {
      // skip processing non target groups
      if (isTargetGroup(group)) {
        for (final Resource resource : group.getResources()) {
          getLog().debug("checking delta for resource: " + resource);
          if (getResourceChangeHandler().isResourceChanged(resource)) {
            getLog().debug("detected change for resource: " + resource + " and group: " + group.getName());
            changedGroupNames.add(group.getName());
            // no need to check rest of resources from this group
            break;
          }
        }
      }
    }
    return changedGroupNames;
  }

  /**
   * Check if the provided group is a target group.
   */
  private boolean isTargetGroup(final Group group) {
    notNull(group);
    final String targetGroups = getTargetGroups();
    // null, means all groups are target groups
    return targetGroups == null || targetGroups.contains(group.getName());
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
      throw new MojoExecutionException("no contextFolder was set!");
    }
  }

  /**
   * Update the classpath.
   */
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
   * @return The {@link TaskExecutor} responsible for running multiple tasks in parallel.
   */
  protected final TaskExecutor<Void> getTaskExecutor() {
    if (taskExecutor == null) {
      taskExecutor = new TaskExecutor<Void>() {
        @Override
        protected void onException(final Exception e) {
          // propagate exception
          throw WroRuntimeException.wrap(e);
        }
      };
    }
    return taskExecutor;
  }

  /**
   * @return true if the only incremental changed group should be used as target groups for next processing.
   */
  protected boolean isIncrementalCheckRequired() {
    return isIncrementalBuild();
  }

  /**
   * Invoked before execution is performed.
   */
  protected void onBeforeExecute() {
  }

  /**
   * Invoked right after execution completion. This method is invoked also if the execution failed with an exception.
   */
  protected void onAfterExecute() {
  }

  /**
   * @return true if the build was triggered by an incremental change.
   */
  protected final boolean isIncrementalBuild() {
    return getResourceChangeHandler().isIncrementalBuild();
  }

  private List<String> getAllModelGroupNames() {
    return new WroModelInspector(getModel()).getGroupNames();
  }

  private WroModel getModel() {
    return getWroManager().getModelFactory().create();
  }

  private WroManager getWroManager() {
    try {
      return getManagerFactory().create();
    } catch (final Exception e) {
      throw WroRuntimeException.wrap(e);
    }
  }

  private ResourceChangeHandler getResourceChangeHandler() {
    if (resourceChangeHandler == null) {
      resourceChangeHandler = ResourceChangeHandler.create(getManagerFactory(), getLog()).setBuildContext(buildContext).setBuildDirectory(
          buildDirectory).setIncrementalBuildEnabled(incrementalBuildEnabled);
    }
    return resourceChangeHandler;
  }

  @VisibleForTesting
  void setTaskExecutor(final TaskExecutor<Void> taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  /**
   * @param contextFolder
   *          the servletContextFolder to set
   * @VisibleForTesting
   */
  String getContextFoldersAsCSV() {
    return contextFolder;
  }

  /**
   * @param contextFolders
   *          a CSV representing contextFolders to use.
   * @VisibleForTesting
   */
  void setContextFolder(final String contextFolder) {
    this.contextFolder = contextFolder;
  }

  /**
   * @param wroFile
   *          the wroFile to set
   * @VisibleForTesting
   */
  void setWroFile(final File wroFile) {
    this.wroFile = wroFile;
  }

  /**
   * @return the wroFile
   * @VisibleForTesting
   */
  File getWroFile() {
    return this.wroFile;
  }

  /**
   * @param minimize
   *          flag for minimization.
   * @VisibleForTesting
   */
  void setMinimize(final boolean minimize) {
    this.minimize = minimize;
  }

  /**
   * @param ignoreMissingResourcesAsString
   *          the ignoreMissingResources to set
   * @VisibleForTesting
   */
  void setIgnoreMissingResources(final String ignoreMissingResourcesAsString) {
    this.ignoreMissingResources = ignoreMissingResourcesAsString;
  }

  void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    setIgnoreMissingResources(Boolean.toString(ignoreMissingResources));
  }

  /**
   * @VisibleForTesting
   */
  protected final boolean isParallelProcessing() {
    return parallelProcessing;
  }

  /**
   * @VisibleForTesting
   */
  final void setParallelProcessing(final boolean parallelProcessing) {
    this.parallelProcessing = parallelProcessing;
  }

  /**
   * @VisibleForTesting
   */
  void setIncrementalBuildEnabled(final boolean incrementalBuildEnabled) {
    this.incrementalBuildEnabled = incrementalBuildEnabled;
  }

  /**
   * @return the minimize
   * @VisibleForTesting
   */
  boolean isMinimize() {
    return this.minimize;
  }

  /**
   * @return the ignoreMissingResources
   * @VisibleForTesting
   */
  String isIgnoreMissingResources() {
    return this.ignoreMissingResources;
  }

  /**
   * Used for testing.
   *
   * @param mavenProject
   *          the mavenProject to set
   */
  void setMavenProject(final MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  /**
   * @return the targetGroups
   * @VisibleForTesting
   */
  String getTargetGroups() {
    return this.targetGroups;
  }

  /**
   * @param versionEncoder
   *          (targetGroups) comma separated group names.
   * @VisibleForTesting
   */
  void setTargetGroups(final String targetGroups) {
    this.targetGroups = targetGroups;
  }

  /**
   * @param wroManagerFactory
   *          fully qualified name of the {@link WroManagerFactory} class.
   * @VisibleForTesting
   */
  void setWroManagerFactory(final String wroManagerFactory) {
    this.wroManagerFactory = wroManagerFactory;
  }

  /**
   * @param extraConfigFile
   *          the extraConfigFile to set
   * @VisibleForTesting
   */
  void setExtraConfigFile(final File extraConfigFile) {
    this.extraConfigFile = extraConfigFile;
  }

  /**
   * @VisibleForTesting
   */
  void setBuildContext(final BuildContext buildContext) {
    this.buildContext = buildContext;
  }

  /**
   * Removes any persisted data creating during the build.
   *
   * @VisibleForTesting
   */
  void clean() {
    try {
      getResourceChangeHandler().destroy();
    } catch (final Exception e) {
      //do not propagate the error during cleanup
      getLog().error("Failed to destroy resourceChangeHandler", e);
    }
  }
}
