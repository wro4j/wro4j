/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * A {@link Runnable} executed by scheduler to reload the cache.
 *
 * @author Alex Objelean
 * @created 24 Oct 2011
 * @since 1.4.2
 */
public final class ReloadCacheRunnable
    implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadCacheRunnable.class);
  private final WeakReference<WroManager> wroManagerReference;


  public ReloadCacheRunnable(final WroManager wroManager) {
    wroManagerReference = new WeakReference<WroManager>(wroManager);
  }

  public void run() {
    try {
      if (true) {
        //Thread.sleep(2000);
        return;
      }
      if (wroManagerReference.get().cacheChangeCallback != null) {
        // invoke cacheChangeCallback
        wroManagerReference.get().cacheChangeCallback.propertyChange(null);
      }
      final WroModel model = wroManagerReference.get().modelFactory.create();
      // process groups & put update cache
      final Collection<Group> groups = model.getGroups();
      // update cache for all resources
      for (final Group group : groups) {
        for (final ResourceType resourceType : ResourceType.values()) {
          if (group.hasResourcesOfType(resourceType)) {
            // TODO check if request parameter can be fetched here without errors.
            // groupExtractor.isMinimized(Context.get().getRequest())
            final Boolean[] minimizeValues = new Boolean[] { true, false };
            for (final boolean minimize : minimizeValues) {
              //stop processing if the current thread is interrupted
              if (Thread.interrupted()) {
                LOG.debug("ReloadCacheRunnable was interrupted - stop processing!");
                throw new InterruptedException();
              }
              final String content = wroManagerReference.get().getGroupsProcessor().process(group, resourceType,
                minimize);
              final CacheEntry cacheEntry = new CacheEntry(group.getName(), resourceType, minimize);
              final ContentHashEntry contentHashEntry = wroManagerReference.get().getContentHashEntryByContent(content);
              wroManagerReference.get().cacheStrategy.put(cacheEntry, contentHashEntry);
            }
          }
        }
      }
    } catch (final InterruptedException e) {
      // Catch all exception in order to avoid situation when scheduler runs out of threads.
      LOG.error("Interrupted exception occured: ", e);
      Thread.currentThread().interrupt();
    } catch (final IOException e) {
      LOG.error("Exception occured during cache reload: ", e);
    } catch (final Exception e) {
      LOG.error("Exception occured during cache reload: ", e);
    }
  }
}