package ro.isdc.wro.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceWatcher;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.SchedulerHelper;


/**
 * Responsible for invoking {@link GroupsProcessor} when cache key is missed.
 * 
 * @author Alex Objelean
 * @crated 2 May 2012
 * @since 1.4.6
 */
public class DefaultSynchronizedCacheStrategyDecorator
    extends AbstractSynchronizedCacheStrategyDecorator<CacheEntry, ContentHashEntry> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultSynchronizedCacheStrategyDecorator.class);
  @Inject
  private GroupsProcessor groupsProcessor;
  @Inject
  private HashStrategy hashBuilder;
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private Injector injector;
  @Inject
  private ReadOnlyContext context;
  private ResourceWatcher resourceWatcher;
  /**
   * Holds the keys that were checked for change. As long as a key is contained in this set, it won't be checked again. 
   */
  private final Set<CacheEntry> checkedKeys = Collections.synchronizedSet(new HashSet<CacheEntry>());
  private SchedulerHelper resourceWatcherScheduler;
  
  /**
   * Decorates the provided {@link CacheStrategy}. The provided {@link CacheStrategy} won't be decorated if the
   * operation is redundant.
   */
  public static CacheStrategy<CacheEntry, ContentHashEntry> decorate(
      final CacheStrategy<CacheEntry, ContentHashEntry> decorated) {
    return decorated instanceof DefaultSynchronizedCacheStrategyDecorator ? decorated
        : new DefaultSynchronizedCacheStrategyDecorator(decorated);
  }
  
  /**
   * @VisibleForTesting
   */
  DefaultSynchronizedCacheStrategyDecorator(final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy) {
    super(cacheStrategy);
    resourceWatcherScheduler = SchedulerHelper.create(new LazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new Runnable() {
          public void run() {
            checkedKeys.clear();
          }
        };
      }
    }, "resourceWatcherScheduler");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected ContentHashEntry loadValue(final CacheEntry key) {
    resourceWatcherScheduler.scheduleWithPeriod(getResourceWatcherUpdatePeriod(), getTimeUnitForResourceWatcher());
    LOG.debug("load value in cache for key: {}", key);
    final String content = groupsProcessor.process(key);
    if (LOG.isDebugEnabled()) {
      LOG.debug("found content: {}", StringUtils.abbreviate(content, 30));
    }
    return computeCacheValueByContent(content);
  }

  private long getResourceWatcherUpdatePeriod() {
    return context.getConfig().getResourceWatcherUpdatePeriod();
  }

  /**
   * @return {@link TimeUnit} used to run resourceWatcher. 
   * @VisibleForTesting
   */
  TimeUnit getTimeUnitForResourceWatcher() {
    return TimeUnit.SECONDS;
  }

  /**
   * Creates a {@link ContentHashEntry} based on provided content.
   */
  private ContentHashEntry computeCacheValueByContent(final String content) {
    String hash = null;
    try {
      if (content != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Content to fingerprint: [{}]", StringUtils.abbreviate(content, 30));
        }
        hash = hashBuilder.getHash(new ByteArrayInputStream(content.getBytes()));
      }
      final ContentHashEntry entry = ContentHashEntry.valueOf(content, hash);
      LOG.debug("computed entry: {}", entry);
      return entry;
    } catch (IOException e) {
      throw new RuntimeException("Should never happen", e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onBeforeGet(final CacheEntry key) {
    if (shouldWatchForChange(key)) {
      LOG.debug("ResourceWatcher check key: {}", key);
      getResourceWatcher().check(key);
      checkedKeys.add(key);
    }
  }
  
  /**
   * @return the {@link ResourceWatcher} instance handling check for stale resources.
   * @VisibleForTesting
   */
  ResourceWatcher getResourceWatcher() {
    if (resourceWatcher == null) {
      resourceWatcher = new ResourceWatcher();
      injector.inject(resourceWatcher);
    }
    return resourceWatcher;
  }

  /**
   * @return true if the provided key should be checked for change. 
   */
  private boolean shouldWatchForChange(final CacheEntry key) {
    LOG.debug("shouldWatchForChange");
    return getResourceWatcherUpdatePeriod() > 0 && !checkedKeys.contains(key);
  }

  @Override
  public void clear() {
    super.clear();
    // reset authorization manager (clear any stored uri's).
    authorizationManager.clear();
  }
}
