package ro.isdc.wro.model.resource.support.change;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.AbstractCssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.util.StopWatch;


/**
 * A runnable responsible for watching if any resources were changed and invalidate the cache entry for the group
 * containing obsolete resources. This class is thread-safe.
 *
 * @author Alex Objelean
 * @created 06 Aug 2012
 * @since 1.4.8
 */
public class ResourceWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceWatcher.class);
  @Inject
  private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private Injector injector;
  private ResourceChangeDetector changeDetector;

  /**
   * Check if resources from a group were changed. If a change is detected, the changeListener will be invoked.
   *
   * @param cacheEntry
   *          the cache key which was requested. The key contains the groupName which has to be checked for changes.
   */
  public void check(final CacheKey cacheEntry) {
    Validate.notNull(cacheEntry);
    LOG.debug("ResourceWatcher started...");
    final StopWatch watch = new StopWatch();
    watch.start("detect changes");
    try {
      final Group group = new WroModelInspector(modelFactory.create()).getGroupByName(cacheEntry.getGroupName());
      if (isGroupChanged(group.collectResourcesOfType(cacheEntry.getType()))) {
        onGroupChanged(cacheEntry);
      }
      changeDetector.reset();
    } catch (final Exception e) {
      onException(e);
    } finally {
      watch.stop();
      LOG.debug("resource watcher info: {}", watch.prettyPrint());
    }
  }

  /**
   * Invoked when exception occurs.
   */
  protected void onException(final Exception e) {
    // not using ERROR log intentionally, since this error is not that important
    LOG.info("Could not check for resource changes because: {}", e.getMessage());
    LOG.debug("[FAIL] detecting resource change ", e);
  }

  private boolean isGroupChanged(final Group group) {
    LOG.debug("Checking if group {} is changed..", group.getName());
    // TODO run the check in parallel?

    final List<Resource> resources = group.getResources();
    boolean isChanged = false;
    for (final Resource resource : resources) {
      if (isChanged = isChanged(resource, group.getName())) {
        onResourceChanged(resource);
      }
    }
    return isChanged;
  }

  /**
   * Invoked when the change of the resource is detected.
   *
   * @param resource
   *          the {@link Resource} which changed.
   * @VisibleForTesting
   */
  void onResourceChanged(final Resource resource) {
  }

  /**
   * Invoked when a resource change detected.
   *
   * @param key
   *          {@link CacheKey} which has to be invalidated because the corresponding group contains stale resources.
   * @VisibleForTesting
   */
  void onGroupChanged(final CacheKey key) {
    LOG.debug("detected change for cacheKey: {}", key);
    cacheStrategy.put(key, null);
  }

  /**
   * Check if the resource was changed from previous run. The implementation uses resource content digest (hash) to
   * check for change.
   *
   * @param resource
   *          the {@link Resource} to check.
   * @return true if the resource was changed.
   */
  private boolean isChanged(final Resource resource, final String groupName) {
    LOG.debug("Check change for resource {}", resource.getUri());
    try {
      final String uri = resource.getUri();
      // using AtomicBoolean because we need to mutate this variable inside an anonymous class.
      final AtomicBoolean changeDetected = new AtomicBoolean(getResourceChangeDetector().checkChangeForGroup(uri,
          groupName));
      if (!changeDetected.get() && resource.getType() == ResourceType.CSS) {
        final Reader reader = new InputStreamReader(locatorFactory.locate(uri));
        LOG.debug("Check @import directive from {}", resource);
        createCssImportProcessor(changeDetected, groupName).process(resource, reader, new StringWriter());
      }
      return changeDetected.get();
    } catch (final IOException e) {
      LOG.debug("[FAIL] Cannot check {} resource (Exception message: {}). Assuming it is unchanged...", resource,
          e.getMessage());
      return false;
    }
  }

  /**
   * @param changeDetected
   *          - flag indicating if the change is detected. When this value is true, the processing will be interrupted
   *          by throwing a {@link RuntimeException}.
   * @param groupName
   *          the name of the group being processed.
   * @return a processor used to detect changes in imported resources.
   */
  private ResourcePreProcessor createCssImportProcessor(final AtomicBoolean changeDetected,
      final String groupName) {
    final ResourcePreProcessor cssImportProcessor = new AbstractCssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String importedUri) {
        LOG.debug("Found @import {}", importedUri);
        final boolean isImportChanged = isChanged(Resource.create(importedUri, ResourceType.CSS), groupName);
        LOG.debug("\tisImportChanged: {}", isImportChanged);
        if (isImportChanged) {
          changeDetected.set(true);
          // we need to continue in order to store the hash for all imported resources, otherwise the change won't be computed correctly.
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
    injector.inject(processor);
    return processor;
  }

  /**
   * @VisibleForTesting
   */
  ResourceChangeDetector getResourceChangeDetector() {
    if (changeDetector == null) {
      changeDetector = new ResourceChangeDetector();
      injector.inject(changeDetector);
    }
    return changeDetector;
  }
}

