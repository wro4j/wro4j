/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link Runnable} executed by scheduler to reload the model.
 *
 * @author Alex Objelean
 * @created 24 Oct 2011
 * @since 1.4.2
 */
public final class ReloadModelRunnable
  implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadModelRunnable.class);

  private final WeakReference<WroManager> wroManagerReference;


  public ReloadModelRunnable(final WroManager wroManager) {
    wroManagerReference = new WeakReference<WroManager>(wroManager);
  }


  public void run() {
    LOG.debug("Reloading Model....");
    try {
      // TODO: do not destroy, until the creation is done and the new model is different than the new one
      wroManagerReference.get().getModelFactory().destroy();
      if (Thread.interrupted()) {
        LOG.debug("ReloadModelRunnable was interrupted - stop processing!");
        throw new InterruptedException();
      }
      wroManagerReference.get().getModelFactory().create();
      wroManagerReference.get().getCacheStrategy().clear();
    } catch (final InterruptedException e) {
      // Catch all exception in order to avoid situation when scheduler runs out of threads.
      LOG.debug("Interrupted exception occured: ", e);
      Thread.currentThread().interrupt();
    } catch (final Exception e) {
      LOG.error("Exception occured during cache reload: ", e);
    }
  }
}