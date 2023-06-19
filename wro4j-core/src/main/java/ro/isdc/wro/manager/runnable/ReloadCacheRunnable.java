/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.runnable;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * A {@link Runnable} executed by scheduler to clear the cache.
 * 
 * @author Alex Objelean
 * @since 1.4.2
 */
public final class ReloadCacheRunnable
    implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadCacheRunnable.class);
  private CacheStrategy<?, ?> cacheStrategy;
  
  public ReloadCacheRunnable(final CacheStrategy<?, ?> cacheStrategy) {
    Validate.notNull(cacheStrategy);
    this.cacheStrategy = cacheStrategy;
  }
  
  public void run() {
    LOG.debug("Reloading Cache....");
    try {
      cacheStrategy.clear();
    } catch (final Exception e) {
      LOG.error("Exception occured during cache reload: ", e);
    }
  }
}