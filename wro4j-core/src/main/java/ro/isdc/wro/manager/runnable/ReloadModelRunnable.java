/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.runnable;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.factory.WroModelFactory;


/**
 * A {@link Runnable} executed by scheduler to clear the model cache.
 * 
 * @author Alex Objelean
 * @created 24 Oct 2011
 * @since 1.4.2
 */
public final class ReloadModelRunnable
    implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadModelRunnable.class);
  private WroModelFactory modelFactory;
  
  public ReloadModelRunnable(final WroModelFactory modelFactory) {
    Validate.notNull(modelFactory);
    this.modelFactory = modelFactory;
  }
  
  public void run() {
    LOG.debug("Reloading Model....");
    try {
      modelFactory.destroy();
    } catch (final Exception e) {
      LOG.error("Exception occured during cache reload: ", e);
    }
  }
}