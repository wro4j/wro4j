package ro.isdc.wro.maven.plugin.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.plugin.logging.Log;
import org.sonatype.plexus.build.incremental.BuildContext;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.AbstractCssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.util.Function;


/**
 * Encapsulates the details about resource change detection and persist the change information in build context.
 *
 * @author Alex Objelean
 * @since 1.7.2
 */
public class ResourceChangeHandler {
  private enum ChangeStatus {
    CHANGED, NOT_CHANGED
  }

  private WroManagerFactory managerFactory;
  private Log log;
  /**
   * Responsible for build storage persistence. Uses configured {@link BuildContext} as a primary storage object.
   */
  private BuildContextHolder buildContextHolder;
  private BuildContext buildContext;
  private File buildDirectory;
  private boolean incrementalBuildEnabled;
  /**
   * Contains the set of already remembered resources. Used to avoid duplicate hash computation.
   */
  private final Set<String> rememberedSet = new HashSet<String>();

  /**
   * Factory method which requires all mandatory fields.
   */
  public static ResourceChangeHandler create(final WroManagerFactory managerFactory, final Log log) {
    notNull(managerFactory, "WroManagerFactory was not set");
    notNull(log, "Log was not set");
    return new ResourceChangeHandler().setManagerFactory(managerFactory).setLog(log);
  }

  private ResourceChangeHandler() {
  }


  public boolean isResourceChanged(final Resource resource) {
    notNull(resource, "Invalid resource provided");

    final WroManager manager = getManagerFactory().create();
    final HashStrategy hashStrategy = manager.getHashStrategy();
    final UriLocatorFactory locatorFactory = manager.getUriLocatorFactory();
    // using AtomicBoolean because we need to mutate this variable inside an anonymous class.
    final AtomicBoolean changeDetected = new AtomicBoolean(false);
    try {
      final String fingerprint = hashStrategy.getHash(locatorFactory.locate(resource.getUri()));
      final String previousFingerprint = getBuildContextHolder().getValue(resource.getUri());

      final boolean newValue = fingerprint != null && !fingerprint.equals(previousFingerprint);
      changeDetected.set(newValue);

      if (!changeDetected.get() && resource.getType() == ResourceType.CSS) {
        final Reader reader = new InputStreamReader(locatorFactory.locate(resource.getUri()));
        getLog().debug("Check @import directive from " + resource);
        // detect changes in imported resources.
        detectChangeForCssImports(resource, reader, changeDetected);
      }
      return changeDetected.get();
    } catch (final IOException e) {
      getLog().error("failed to check for delta resource: " + resource, e);
    }
    return false;
  }

  private void detectChangeForCssImports(final Resource resource, final Reader reader,
      final AtomicBoolean changeDetected)
      throws IOException {
    forEachCssImportApply(new Function<String, ChangeStatus>() {
      public ChangeStatus apply(final String importedUri) throws Exception {
        final boolean isImportChanged = isResourceChanged(Resource.create(importedUri, ResourceType.CSS));
        getLog().debug("\tisImportChanged: " + isImportChanged);
        if (isImportChanged) {
          changeDetected.set(true);
          return ChangeStatus.CHANGED;
        }
        return ChangeStatus.NOT_CHANGED;
      }
    }, resource, reader);
  }

  /**
   * Will persist the information regarding the provided resource in some internal store. This information will be used
   * later to check if the resource is changed.
   *
   * @param resource
   *          {@link Resource} to touch.
   */
  public void remember(final Resource resource) {
    final WroManager manager = getManagerFactory().create();
    final HashStrategy hashStrategy = manager.getHashStrategy();
    final UriLocatorFactory locatorFactory = manager.getUriLocatorFactory();

    if (rememberedSet.contains(resource.getUri())) {
      // only calculate fingerprints and check imports if not already done
      getLog().debug("Resource with uri '" + resource.getUri() + "' has already been updated in this run.");
    } else {
      try {
        final String fingerprint = hashStrategy.getHash(locatorFactory.locate(resource.getUri()));
        getBuildContextHolder().setValue(resource.getUri(), fingerprint);
        rememberedSet.add(resource.getUri());
        getLog().debug("Persist fingerprint for resource '" + resource.getUri() + "' : " + fingerprint);
        if (resource.getType() == ResourceType.CSS) {
          final Reader reader = new InputStreamReader(locatorFactory.locate(resource.getUri()));
          getLog().debug("Check @import directive from " + resource);
          // persist fingerprints in imported resources.
          persistFingerprintsForCssImports(resource, reader);
        }
      } catch (final IOException e) {
        getLog().debug("could not check fingerprint of resource: " + resource);
      }
    }
  }

  private void persistFingerprintsForCssImports(final Resource resource, final Reader reader)
      throws IOException {
    forEachCssImportApply(new Function<String, ChangeStatus>() {
      public ChangeStatus apply(final String importedUri) throws Exception {
        remember(Resource.create(importedUri, ResourceType.CSS));
        return ChangeStatus.NOT_CHANGED;
      }
    }, resource, reader);
  }

  /**
   * Invokes the provided function for each detected css import.
   *
   * @param func
   *          a function (closure) invoked for each found import. It will be provided as argument the uri of imported
   *          css.
   */
  private void forEachCssImportApply(final Function<String, ChangeStatus> func, final Resource resource, final Reader reader)
      throws IOException {
    final ResourcePreProcessor processor = createCssImportProcessor(func);
    InjectorBuilder.create(getManagerFactory()).build().inject(processor);
    processor.process(resource, reader, new StringWriter());
  }

  private ResourcePreProcessor createCssImportProcessor(final Function<String, ChangeStatus> func) {
    final ResourcePreProcessor cssImportProcessor = new AbstractCssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String importedUri) {
        getLog().debug("Found @import " + importedUri);
        try {
          final ChangeStatus status = func.apply(importedUri);
          getLog().debug("ChangeStatus for " + importedUri + ": " + status);
          if (ChangeStatus.NOT_CHANGED.equals(status)) {
        	  remember(Resource.create(importedUri, ResourceType.CSS));
          }
        } catch (final Exception e) {
          getLog().error("Cannot apply a function on @import resource: " + importedUri + ". Ignoring it.", e);
        }
        remember(Resource.create(importedUri, ResourceType.CSS));
      }

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
    return new ExceptionHandlingProcessorDecorator(cssImportProcessor) {
      @Override
      protected boolean isIgnoreFailingProcessor() {
        return true;
      }
    };
  }

  private BuildContextHolder getBuildContextHolder() {
    if (buildContextHolder == null) {
      buildContextHolder = new BuildContextHolder(buildContext, buildDirectory);
      buildContextHolder.setIncrementalBuildEnabled(incrementalBuildEnabled);
    }
    return buildContextHolder;
  }

  void setBuildContextHolder(final BuildContextHolder buildContextHolder) {
    this.buildContextHolder = buildContextHolder;
  }

  private WroManagerFactory getManagerFactory() {
    return managerFactory;
  }

  public Log getLog() {
    return log;
  }

  public ResourceChangeHandler setManagerFactory(final WroManagerFactory wroManagerFactory) {
    this.managerFactory = wroManagerFactory;
    return this;
  }

  public ResourceChangeHandler setLog(final Log log) {
    this.log = log;
    return this;
  }

  public ResourceChangeHandler setBuildContext(final BuildContext buildContext) {
    this.buildContext = buildContext;
    return this;
  }

  public ResourceChangeHandler setBuildDirectory(final File buildDirectory) {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public ResourceChangeHandler setIncrementalBuildEnabled(final boolean incrementalBuildEnabled) {
    this.incrementalBuildEnabled = incrementalBuildEnabled;
    return this;
  }

  public boolean isIncrementalBuild() {
    return getBuildContextHolder().isIncrementalBuild();
  }

  /**
   * Destroys all information about resource tracked for changes.
   */
  public void destroy() {
    getBuildContextHolder().destroy();
    rememberedSet.clear();
  }

  /**
   * After invoking this method on a resource, the next invocation of {@link #isResourceChanged(Resource)} will return
   * true.
   *
   * @param resource
   *          The resource to clear from persisted storage.
   */
  public void forget(final Resource resource) {
    if (resource != null) {
      getBuildContextHolder().setValue(resource.getUri(), null);
      rememberedSet.remove(resource.getUri());
    }
  }

  /**
   * Persist the values stored in BuildContext(Holder)
   */
  public void persist() {
    getBuildContextHolder().persist();
  }
}
