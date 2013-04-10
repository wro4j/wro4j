/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.AbstractCssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;


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
   * The folder where web application context resides useful for locating resources relative to servletContext .
   *
   * @parameter default-value="${basedir}/src/main/webapp/" expression="${contextFolder}"
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
   * {@inheritDoc}
   */
  public final void execute()
      throws MojoExecutionException {
    validate();
    getLog().info("Executing the mojo: ");
    getLog().info("Wro4j Model path: " + wroFile.getPath());
    getLog().info("targetGroups: " + getTargetGroups());
    getLog().info("minimize: " + isMinimize());
    getLog().info("ignoreMissingResources: " + isIgnoreMissingResources());
    getLog().debug("wroManagerFactory: " + this.wroManagerFactory);
    getLog().debug("extraConfig: " + extraConfigFile);

    extendPluginClasspath();
    Context.set(Context.standaloneContext());
    try {
      onBeforeExecute();
      doExecute();
    } catch (final Exception e) {
      throw new MojoExecutionException("Exception occured while processing: " + e.getMessage(), e);
    } finally {
      onAfterExecute();
    }
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
   * Creates a {@link StandaloneContext} by setting properties passed after mojo is initialized.
   */
  private StandaloneContext createStandaloneContext() {
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
  protected abstract void doExecute()
      throws Exception;

  /**
   * This method will ensure that you have a right and initialized instance of {@link StandaloneContextAware}. When
   * overriding this method, ensure that creating managerFactory performs injection during manager creation, otherwise
   * the manager won't be initialized porperly.
   *
   * @return {@link WroManagerFactory} implementation.
   */
  protected WroManagerFactory getManagerFactory() {
    if (managerFactory == null) {
      try {
        managerFactory = newWroManagerFactory();
      } catch (final MojoExecutionException e) {
        throw WroRuntimeException.wrap(e);
      }
      // initialize before process.
      if (managerFactory instanceof StandaloneContextAware) {
        ((StandaloneContextAware) managerFactory).initialize(createStandaloneContext());
      }
    }
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
      getLog().error("Cannot instantiate wroManagerFactoryClass", e);
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
    if (isIncrementalBuild()) {
      result = getIncrementalGroupNames();
    } else if (getTargetGroups() == null) {
      result = getAllModelGroupNames();
    } else {
      result = Arrays.asList(getTargetGroups().split(","));
    }
    persistResourceFingerprints(result);
    getLog().info("The following groups will be processed: " + result);
    return result;
  }

  /**
   * Store digest for all resources contained inside the list of provided groups.
   */
  private void persistResourceFingerprints(final List<String> groupNames) {
    if (buildContext != null) {
      final WroModelInspector modelInspector = new WroModelInspector(getModel());
      final WroManager manager = getWroManager();
      final HashStrategy hashStrategy = manager.getHashStrategy();
      final UriLocatorFactory locatorFactory = manager.getUriLocatorFactory();
      for (final String groupName : groupNames) {
        final Group group = modelInspector.getGroupByName(groupName);
        if (group != null) {
          for (final Resource resource : group.getResources()) {
            try {
              final String fingerprint = hashStrategy.getHash(locatorFactory.locate(resource.getUri()));
              buildContext.setValue(resource.getUri(), fingerprint);
            } catch (final IOException e) {
              getLog().debug("could not check fingerprint of resource: " + resource);
            }
          }
        }
      }
    }
  }

  private ResourcePreProcessor createCssImportProcessor(final Resource resource, final AtomicBoolean changeDetected) {
    final ResourcePreProcessor cssImportProcessor = new AbstractCssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String importedUri) {
        getLog().debug("Found @import " + importedUri);
        final boolean isImportChanged = isResourceChanged(Resource.create(importedUri, ResourceType.CSS));
        getLog().debug("\tisImportChanged: " + isImportChanged);
        if (isImportChanged) {
          changeDetected.set(true);
          // no need to continue
          throw new WroRuntimeException("Change detected. No need to continue processing");
        }
      };

      @Override
      protected String doTransform(final String cssContent, final List<Resource> foundImports)
          throws IOException {
        // no need to build the content, since we are interested in finding imported resources only
        return "";
      }

      @Override
      public String toString() {
        return CssImportPreProcessor.class.getSimpleName();
      }
    };
    /**
     * Ignore processor failure, since we are interesting in detecting change only. A failure is treated as lack of
     * change.
     */
    final ResourcePreProcessor processor = new ExceptionHandlingProcessorDecorator(cssImportProcessor) {
      @Override
      protected boolean isIgnoreFailingProcessor() {
        return true;
      }
    };
    InjectorBuilder.create(getManagerFactory()).build().inject(processor);
    return processor;
  }

  /**
   * @return a list of groups changed by incremental builds.
   */
  private List<String> getIncrementalGroupNames()
      throws Exception {
    final List<String> changedGroupNames = new ArrayList<String>();
    String targetGroups = getTargetGroups();
    List<String> targetGroupsList = Collections.emptyList();
    if (getTargetGroups() != null) {
      targetGroupsList = Arrays.asList(getTargetGroups().split(","));
    }
    for (final Group group : getModel().getGroups()) {
      if (! targetGroups.isEmpty() && !targetGroups.contains(group.getName())) {
        //skip this group processing
        continue;
      }
      for (final Resource resource : group.getResources()) {
        getLog().debug("checking delta for resource: " + resource);
        if (isResourceChanged(resource)) {
          getLog().debug("detected change for resource: " + resource + " and group: " + group.getName());
          changedGroupNames.add(group.getName());
          // no need to check rest of resources from this group
          break;
        }
      }
    }
    return changedGroupNames;
  }

  private boolean isResourceChanged(final Resource resource) {
    final WroManager manager = getWroManager();
    final HashStrategy hashStrategy = manager.getHashStrategy();
    final UriLocatorFactory locatorFactory = manager.getUriLocatorFactory();
    // using AtomicBoolean because we need to mutate this variable inside an anonymous class.
    final AtomicBoolean changeDetected = new AtomicBoolean(false);
    try {
      final String fingerprint = hashStrategy.getHash(locatorFactory.locate(resource.getUri()));
      final String previousFingerprint = buildContext != null ? String
          .valueOf(buildContext.getValue(resource.getUri())) : null;
      getLog().debug("fingerprint <current, prev>: <" + fingerprint + ", " + previousFingerprint + ">");

      changeDetected.set(fingerprint != null && !fingerprint.equals(previousFingerprint));

      if (!changeDetected.get() && resource.getType() == ResourceType.CSS) {
        final Reader reader = new InputStreamReader(locatorFactory.locate(resource.getUri()));
        getLog().debug("Check @import directive from " + resource);
        // detect changes in imported resources.
        createCssImportProcessor(resource, changeDetected).process(resource, reader, new StringWriter());
      }
      return changeDetected.get();
    } catch (final IOException e) {
      getLog().debug("failed to check for delta resource: " + resource);
    }
    return false;
  }

  protected final boolean isIncrementalBuild() {
    return buildContext != null && buildContext.isIncremental();
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
   * @param contextFolder
   *          the servletContextFolder to set
   * @VisibleForTesting
   */
  void setContextFolder(final File contextFolder) {
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
   * @return the contextFolder
   * @VisibleForTesting
   */
  File getContextFolder() {
    return this.contextFolder;
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
   * @param ignoreMissingResources
   *          the ignoreMissingResources to set
   * @VisibleForTesting
   */
  void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    this.ignoreMissingResources = ignoreMissingResources;
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
  boolean isIgnoreMissingResources() {
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
}
