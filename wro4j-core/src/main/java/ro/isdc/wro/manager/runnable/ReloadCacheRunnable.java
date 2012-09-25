/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.runnable;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.manager.WroManager;


/**
 * A {@link Runnable} executed by scheduler to clear the cache.
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
    LOG.debug("Reloading Cache....");
    try {
      wroManagerReference.get().getCacheStrategy().clear();
    } catch (final Exception e) {
      LOG.error("Exception occured during cache reload: ", e);
    }
  }
}