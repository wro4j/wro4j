package ro.isdc.wro.model.resource.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
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
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
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
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private HashStrategy hashStrategy;
  @Inject
  private Injector injector;
  /**
   * Map between a resource uri and a corresponding {@link ResourceInfo} object.
   */
  private final Map<String, ResourceInfo> resourceInfoMap = new ConcurrentHashMap<String, ResourceInfo>() {
    @Override
    public ResourceInfo get(final Object key) {
      ResourceInfo result = super.get(key);
      if (result == null) {
        result = new ResourceInfo();
        put((String) key, result);
      }
      return result;
    }
  };

  /**
   * Holds details about hashes of watched resources and the group which were detected as changed.
   */
  private class ResourceInfo {
    private String currentHash;
    private String prevHash;
    private final Set<String> groups;

    public ResourceInfo() {
      groups = new HashSet<String>();
    }

    public void updateHashForGroup(final String currentHash, final String groupName) {
      this.currentHash = currentHash;
      groups.clear();
      groups.add(groupName);
    }

    public void cleanUp() {
      System.out.println("cleanUp " + this);
      this.prevHash = currentHash;
      this.currentHash = null;
    }

    public boolean isChanged(final String groupName) {
      final boolean result = prevHash != null && (prevHash.equals(currentHash) ? groups.contains(groupName) : true);
      LOG.info("resourceInfo: {}, changed: {}", this, result);
      return result;
    }

    public boolean isCheckRequiredForGroup(final String groupName) {
      groups.add(groupName);
      return currentHash == null;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }
  }

  /**
   * Check if resources from a group were changed. If a change is detected, the changeListener will be invoked.
   *
   * @param cacheEntry
   *          the cache key which was requested. The key contains the groupName which has to be checked for changes.
   */
  public void check(final CacheEntry cacheEntry) {
    Validate.notNull(cacheEntry);
    LOG.debug("ResourceWatcher started...");
    final StopWatch watch = new StopWatch();
    watch.start("detect changes");
    try {
      final Group group = modelFactory.create().getGroupByName(cacheEntry.getGroupName());
      if (isGroupChanged(group)) {
        onGroupChanged(cacheEntry);
      }
      System.err.println("cleaningUp: " + resourceInfoMap.values());
      for (final ResourceInfo resourceInfo : resourceInfoMap.values()) {
        resourceInfo.cleanUp();
      }
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
    LOG.info("Could not chef for resource changes because: {}", e.getMessage());
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
        break;
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
   *          {@link CacheEntry} which has to be invalidated because the corresponding group contains stale resources.
   * @VisibleForTesting
   */
  void onGroupChanged(final CacheEntry key) {
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
      final ResourceInfo resourceInfo = updateCurrentHash(uri, groupName);
      // using AtomicBoolean because we need to mutate this variable inside an anonymous class.
      final AtomicBoolean changeDetected = new AtomicBoolean(resourceInfo.isChanged(groupName));
      if (!changeDetected.get() && resource.getType() == ResourceType.CSS) {
        final Reader reader = new InputStreamReader(locatorFactory.locate(uri));
        LOG.debug("Check @import directive from {}", resource);
        // detect changes in imported resources.
        createCssImportProcessor(resource, changeDetected, groupName).process(resource, reader, new StringWriter());
      }
      return changeDetected.get();
    } catch (final IOException e) {
      LOG.debug("[FAIL] Cannot check {} resource (Exception message: {}). Assuming it is unchanged...", resource,
          e.getMessage());
      return false;
    }
  }

  private ResourcePreProcessor createCssImportProcessor(final Resource resource, final AtomicBoolean changeDetected,
      final String groupName) {
    final ResourcePreProcessor cssImportProcessor = new AbstractCssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String importedUri) {
        LOG.debug("Found @import {}", importedUri);
        final boolean isImportChanged = isChanged(Resource.create(importedUri, ResourceType.CSS), groupName);
        LOG.debug("\tisImportChanged: {}", isImportChanged);
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
    injector.inject(processor);
    return processor;
  }

  /**
   * @param uri
   *          of the resource to get the hash for.
   * @return the hash for a given resource uri.
   * @throws IOException
   */
  private ResourceInfo updateCurrentHash(final String uri, final String groupName)
      throws IOException {
    final ResourceInfo resourceInfo = resourceInfoMap.get(uri);
    if (resourceInfo.isCheckRequiredForGroup(groupName)) {
      LOG.debug("Checking if resource {} is changed..", uri);
      final String currentHash = hashStrategy.getHash(new AutoCloseInputStream(locatorFactory.locate(uri)));
      resourceInfo.updateHashForGroup(currentHash, groupName);
    }
    return resourceInfo;
  }

  /**
   * @return the map storing the hash of accumulated resources from previous runs.
   * @VisibleForTesting
   */
  Map<String, String> getPreviousHashes() {
    // return Collections.unmodifiableMap(previousHashes);
    return null;
  }

  /**
   * @return the map storing the hash of resources from current run.
   * @VisibleForTesting
   */
  Map<String, String> getCurrentHashes() {
    // return Collections.unmodifiableMap(currentHashes);
    return null;
  }
}
