package ro.isdc.wro.cache.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.SchedulerHelper;


/**
 * Responsible for invoking {@link GroupsProcessor} when cache key is missed.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public class DefaultSynchronizedCacheStrategyDecorator
    extends AbstractSynchronizedCacheStrategyDecorator<CacheKey, CacheValue> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultSynchronizedCacheStrategyDecorator.class);
  @Inject
  private GroupsProcessor groupsProcessor;
  @Inject
  private HashStrategy hashStrategy;
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private ReadOnlyContext context;
  @Inject
  private ResourceWatcher resourceWatcher;

  /**
   * Holds the keys that were checked for change. As long as a key is contained in this set, it won't be checked again.
   */
  private final Set<CacheKey> checkedKeys = Collections.synchronizedSet(new HashSet<CacheKey>());
  private final SchedulerHelper resourceWatcherScheduler;

  /**
   * Decorates the provided {@link CacheStrategy}. The provided {@link CacheStrategy} won't be decorated if the
   * operation is redundant.
   */
  public static CacheStrategy<CacheKey, CacheValue> decorate(final CacheStrategy<CacheKey, CacheValue> decorated) {
    return decorated instanceof DefaultSynchronizedCacheStrategyDecorator ? decorated
        : new DefaultSynchronizedCacheStrategyDecorator(decorated);
  }

  /**
   * Based on provided {@link CacheKey} a new key is created which has the same value. This is useful to avoid hashCode
   * variation for minimize flag. This does make sense for resource watcher functionality, when the changes for original
   * resources are performed.
   */
  private static CacheKey createIgnoreMinimizeFlagKey(final CacheKey cacheKey) {
    return new CacheKey(cacheKey.getGroupName(), cacheKey.getType());
  }

  /**
   * @VisibleForTesting
   */
  DefaultSynchronizedCacheStrategyDecorator(final CacheStrategy<CacheKey, CacheValue> cacheStrategy) {
    super(cacheStrategy);
    resourceWatcherScheduler = newResourceWatcherScheduler();
  }

  /**
   * @VisibleForTesting
   */
  SchedulerHelper newResourceWatcherScheduler() {
    return SchedulerHelper.create(new LazyInitializer<Runnable>() {
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

  @Override
  protected CacheValue loadValue(final CacheKey key) {
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
   * Creates a {@link CacheValue} based on provided content.
   */
  private CacheValue computeCacheValueByContent(final String content) {
    String hash = null;
    try {
      if (content != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Content to fingerprint: [{}]", StringUtils.abbreviate(content, 30));
        }
        hash = hashStrategy.getHash(new ByteArrayInputStream(content.getBytes()));
      }
      final CacheValue entry = CacheValue.valueOf(content, hash);
      LOG.debug("computed entry: {}", entry);
      return entry;
    } catch (final IOException e) {
      throw new RuntimeException("Should never happen", e);
    }
  }

  @Override
  protected void onBeforeGet(final CacheKey key) {
    if (shouldWatchForChange(key)) {
      LOG.debug("tryAsyncCheck");
      if (resourceWatcher.tryAsyncCheck(key)) {
        checkedKeys.add(createIgnoreMinimizeFlagKey(key));
      }
    }
  }

  /**
   * @return true if the provided key should be checked for change.
   */
  private boolean shouldWatchForChange(final CacheKey key) {
    final boolean result = getResourceWatcherUpdatePeriod() > 0 && !wasCheckedForChange(key);
    LOG.debug("shouldWatchForChange={}", result);
    return result;
  }

  @Override
  public void clear() {
    super.clear();
    // reset authorization manager (clear any stored uri's).
    if (authorizationManager instanceof MutableResourceAuthorizationManager) {
      ((MutableResourceAuthorizationManager) authorizationManager).clear();
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    resourceWatcherScheduler.destroy();
  }

  /**
   * @return true if the provided key was checked for change since last resourceWatcher update period iteration started.
   * @VisibleForTesting
   */
  boolean wasCheckedForChange(final CacheKey key) {
    return checkedKeys.contains(key);
  }
}
