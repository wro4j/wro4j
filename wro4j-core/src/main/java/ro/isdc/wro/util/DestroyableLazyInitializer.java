/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.processor.Destroyable;


/**
 * A subclass of {@link LazyInitializer} allows to destroy the created object.
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
public abstract class DestroyableLazyInitializer<T> extends LazyInitializer<T> {
  private static final Logger LOG = LoggerFactory.getLogger(DestroyableLazyInitializer.class);
  /**
   * Destroy the initialized object. This will trigger the re-initialization when
   * {@link DestroyableLazyInitializer#get()} method is invoked.
   */
  public void destroy() {
    if (isInitialized()) {
      if (get() instanceof Destroyable) {
        try {
          ((Destroyable) get()).destroy();
        } catch (final Exception e) {
          LOG.error("destroy operation failed", e);
        }
      }
    }
    object = null;
  }
}
