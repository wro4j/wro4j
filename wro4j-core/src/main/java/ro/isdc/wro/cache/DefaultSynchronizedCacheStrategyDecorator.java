package ro.isdc.wro.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.util.HashBuilder;

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
  private HashBuilder hashBuilder; 
  
  public DefaultSynchronizedCacheStrategyDecorator(final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy) {
    super(cacheStrategy);
  }
  
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected ContentHashEntry loadValue(final CacheEntry key) {
    final String content = groupsProcessor.process(key);
    return computeCacheValueByContent(content);
  }

  /**
   * Creates a {@link ContentHashEntry} based on provided content.
   */
  private ContentHashEntry computeCacheValueByContent(final String content) {
    String hash = null;
    try {
      if (content != null) {
        LOG.debug("Content to fingerprint: [{}]", StringUtils.abbreviate(content, 40));
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
  public void put(final CacheEntry key, final ContentHashEntry value) {
    if (!Context.get().getConfig().isDisableCache()) {
      super.put(key, value);
    }
  }
}
