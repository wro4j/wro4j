package ro.isdc.wro.manager.runnable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.util.StopWatch;


/**
 * A runnable responsible for watching if any resources were changed and invalidate the cache entry for the group
 * containing obsolete resources.
 * 
 * @author Alex Objelean
 * @created 06 Aug 2012
 * @since 1.4.8
 */
public class ResourceWatcherRunnable
    implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceWatcherRunnable.class);
  @Inject
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private HashStrategy hashStrategy;
  /**
   * Contains the resource uri's with associated hash values retrieved from last successful check.
   */
  private final Map<String, String> previousHashes = new HashMap<String, String>();
  /**
   * Contains the resource uri's with associated hash values retrieved from currently performed check.
   */
  private final Map<String, String> currentHashes = new HashMap<String, String>();

  public ResourceWatcherRunnable(final Injector injector) {
    Validate.notNull(injector);
    injector.inject(this);
  }
  
  /**
   * {@inheritDoc}
   */
  public void run() {
    LOG.debug("ResourceWatcher started...");
    final StopWatch watch = new StopWatch();
    watch.start("detect changes");
    try {
      final Collection<Group> groups = modelFactory.create().getGroups();
      // TODO run the check in parallel?
      for (final Group group : groups) {
        checkForChanges(group);
      }
      // cleanUp
      for (Entry<String, String> entry : currentHashes.entrySet()) {
        previousHashes.put(entry.getKey(), entry.getValue());
      }
      currentHashes.clear();
    } catch (Exception e) {
      LOG.error("Exception while checking for resource changes", e);
    } finally {
      watch.stop();
      LOG.debug("resource watcher info: {}", watch.prettyPrint());
    }
  }
  
  private void checkForChanges(final Group group) {
    LOG.debug("Checking if group {} is changed..", group.getName());
    final List<Resource> resources = group.getResources();
    for (Resource resource : resources) {
      if (isChanged(resource)) {
        onResourceChanged(group, resource);
        // no need to check the rest of resources
        break;
      }
    }
  }
  
  /**
   * Invoked when a resource change detected.
   * 
   * @param group {@link Group} to which the changed {@link Resource} belongs to.
   * @param resource the changed {@link Resource}.
   * @VisibleForTesting
   */
  void onResourceChanged(final Group group, final Resource resource) {
    invalidate(group, resource);    
  }

  /**
   * Check if the resource was changed from previous run. The implementation uses resource content digest (hash) to
   * check for change.
   * 
   * @param resource
   *          the {@link Resource} to check.
   * @return true if the resource was changed.
   */
  private boolean isChanged(final Resource resource) {
    LOG.debug("Checking if resource {} is changed..", resource);
    try {
      final String uri = resource.getUri();
      String currentHash = getCurrentHash(uri);
      final String lastHash = previousHashes.get(uri);
      return lastHash != null ? !lastHash.equals(currentHash) : false;
    } catch (IOException e) {
      LOG.debug("[FAIL] Cannot check {} resource (Exception message: {}). Assuming it is unchanged...", resource,
          e.getMessage());
      return false;
    }
  }
  
  /**
   * @param uri
   *          of the resource to get the hash for.
   * @return the hash for a given resource uri.
   * @throws IOException
   */
  private String getCurrentHash(final String uri)
      throws IOException {
    String currentHash = currentHashes.get(uri);
    if (currentHash == null) {
      currentHash = hashStrategy.getHash(locatorFactory.locate(uri));
      currentHashes.put(uri, currentHash);
    }
    return currentHash;
  }
  
  /**
   * Removes from cache the entry for the obsolete group.
   * 
   * @param group
   *          the group whose resources are obsolete.
   * @param resourceType
   *          the {@link ResourceType} of the obsolete resource required to remove the exact entry from the cache.
   */
  private void invalidate(final Group group, final Resource resource) {
    LOG.debug("Detected change for {} resource. Invalidating group: {}", resource.getUri(), group.getName());
    // Invalidate the entry by putting NULL into the cache
    CacheEntry key = new CacheEntry(group.getName(), resource.getType(), true);
    cacheStrategy.put(key, null);
    key = new CacheEntry(group.getName(), resource.getType(), false);
    cacheStrategy.put(key, null);
  }
  
  /**
   * @return the map storing the hash of accumulated resources from previous runs.
   * @VisibleForTesting
   */
  Map<String, String> getPreviousHashes() {
    return Collections.unmodifiableMap(previousHashes);
  }
  
  /**
   * @return the map storing the hash of resources from current run.
   * @VisibleForTesting
   */
  Map<String, String> getCurrentHashes() {
    return Collections.unmodifiableMap(currentHashes);
  }
}
