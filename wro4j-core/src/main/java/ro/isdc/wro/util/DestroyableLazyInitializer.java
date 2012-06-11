/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;


/**
 * A subclass of {@link LazyInitializer} allows to destroy the created object.
 *
 * @author Alex Objelean
 * @created 24 Oct 2011
 * @since 1.4.2
 */
public abstract class DestroyableLazyInitializer<T> extends LazyInitializer<T> {
  /**
   * Destroy the initialized object. This will trigger the re-initialization when
   * {@link DestroyableLazyInitializer#get()} method is invoked.
   */
  public final void destroy() {
    object = null;
  }
}
