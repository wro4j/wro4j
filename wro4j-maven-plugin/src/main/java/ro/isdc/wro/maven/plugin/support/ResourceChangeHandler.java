package ro.isdc.wro.maven.plugin.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.plugin.logging.Log;
import org.sonatype.plexus.build.incremental.BuildContext;

import ro.isdc.wro.WroRuntimeException;
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

public class ResourceChangeHandler {
  private WroManagerFactory managerFactory;
  /**
   * Responsible for build storage persistence. Uses configured {@link BuildContext} as a primary storage object.
   */
  private BuildContextHolder buildContextHolder;
  private Log log;
  private BuildContext buildContext;
  private File buildDirectory;
  private boolean incrementalBuildEnabled;

  public boolean isResourceChanged(final Resource resource) {
    notNull(managerFactory);
    notNull(buildContextHolder);
    notNull(log);

    final WroManager manager = getManagerFactory().create();
    final HashStrategy hashStrategy = manager.getHashStrategy();
    final UriLocatorFactory locatorFactory = manager.getUriLocatorFactory();
    // using AtomicBoolean because we need to mutate this variable inside an anonymous class.
    final AtomicBoolean changeDetected = new AtomicBoolean(false);
    try {
      final String fingerprint = hashStrategy.getHash(locatorFactory.locate(resource.getUri()));
      final String previousFingerprint = getBuildContextHolder().getValue(resource.getUri());
      getLog().debug("fingerprint <current, prev>: <" + fingerprint + ", " + previousFingerprint + ">");

      changeDetected.set(fingerprint != null && !fingerprint.equals(previousFingerprint));

      if (!changeDetected.get() && resource.getType() == ResourceType.CSS) {
        final Reader reader = new InputStreamReader(locatorFactory.locate(resource.getUri()));
        getLog().debug("Check @import directive from " + resource);
        // detect changes in imported resources.
        detectChangeForCssImports(resource, reader, changeDetected);
      }
      return changeDetected.get();
    } catch (final IOException e) {
      getLog().debug("failed to check for delta resource: " + resource);
    }
    return false;
  }


  private void detectChangeForCssImports(final Resource resource, final Reader reader,
      final AtomicBoolean changeDetected)
      throws IOException {
    forEachCssImportApply(new Function<String, Void>() {
      public Void apply(final String importedUri)
          throws Exception {
        final boolean isImportChanged = isResourceChanged(Resource.create(importedUri, ResourceType.CSS));
        getLog().debug("\tisImportChanged: " + isImportChanged);
        if (isImportChanged) {
          changeDetected.set(true);
          // no need to continue
          throw new WroRuntimeException("Change detected. No need to continue processing");
        }
        return null;
      }
    }, resource, reader);
  }


  public void persistResourceFingerprints(final Resource resource) {
    final WroManager manager = getManagerFactory().create();
    final HashStrategy hashStrategy = manager.getHashStrategy();
    final UriLocatorFactory locatorFactory = manager.getUriLocatorFactory();
    try {
      final String fingerprint = hashStrategy.getHash(locatorFactory.locate(resource.getUri()));
      getBuildContextHolder().setValue(resource.getUri(), fingerprint);
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


  private void persistFingerprintsForCssImports(final Resource resource, final Reader reader)
      throws IOException {
    forEachCssImportApply(new Function<String, Void>() {
      public Void apply(final String importedUri)
          throws Exception {
        persistResourceFingerprints(Resource.create(importedUri, ResourceType.CSS));
        return null;
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
  private void forEachCssImportApply(final Function<String, Void> func, final Resource resource, final Reader reader)
      throws IOException {
    final ResourcePreProcessor processor = createCssImportProcessor(func);
    InjectorBuilder.create(getManagerFactory()).build().inject(processor);
    processor.process(resource, reader, new StringWriter());
  }


  private ResourcePreProcessor createCssImportProcessor(final Function<String, Void> func) {
    final ResourcePreProcessor cssImportProcessor = new AbstractCssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String importedUri) {
        getLog().debug("Found @import " + importedUri);
        try {
          func.apply(importedUri);
        } catch (final Exception e) {
          getLog().error("Cannot apply a function on @import resource: " + importedUri + ". Ignoring it.", e);
        }
        persistResourceFingerprints(Resource.create(importedUri, ResourceType.CSS));
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
    final ResourcePreProcessor processor = new ExceptionHandlingProcessorDecorator(cssImportProcessor) {
      @Override
      protected boolean isIgnoreFailingProcessor() {
        return true;
      }
    };
    return processor;
  }

  private BuildContextHolder getBuildContextHolder() {
    if (buildContextHolder == null) {
      buildContextHolder = new BuildContextHolder(buildContext, buildDirectory);
      buildContextHolder.setIncrementalBuildEnabled(incrementalBuildEnabled);
    }
    return buildContextHolder;
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


  public void destroy() {
    getBuildContextHolder().destroy();
  }
}
