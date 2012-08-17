package ro.isdc.wro.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceWatcher;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * Responsible for invoking {@link GroupsProcessor} when cache key is missed.
 *   
 * @author Alex Objelean
 * @crated 2 May 2012
 * @since 1.4.6
 */
public class DefaultSynchronizedCacheStrategyDecorator extends AbstractSynchronizedCacheStrategyDecorator<CacheEntry, ContentHashEntry> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultSynchronizedCacheStrategyDecorator.class);
  @Inject
  private GroupsProcessor groupsProcessor;
  @Inject
  private HashStrategy hashBuilder; 
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private Injector injector;
  private ResourceWatcher resourceWather;
  
  public DefaultSynchronizedCacheStrategyDecorator(final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy) {
    super(cacheStrategy);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected ContentHashEntry loadValue(final CacheEntry key) {
    LOG.debug("load value in cache for key: {}", key);
    final String content = groupsProcessor.process(key);
    if (LOG.isDebugEnabled()) {
      LOG.debug("found content: {}", StringUtils.abbreviate(content, 30));
    }
    return computeCacheValueByContent(content);
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
    if (shouldWatchForChange()) {
      getResourceWatcher().check(key.getGroupName());
    }
  }
  
  private ResourceWatcher getResourceWatcher() {
    if (resourceWather == null) {
      resourceWather = new ResourceWatcher();
      injector.inject(resourceWather);
    }
    return null;
  }

  private boolean shouldWatchForChange() {
    return false;
  }

  @Override
  public void clear() {
    super.clear();
    //reset authorization manager (clear any stored uri's).
    authorizationManager.clear();
  }
}
