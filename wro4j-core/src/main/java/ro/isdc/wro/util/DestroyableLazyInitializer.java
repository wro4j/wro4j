/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;

import org.apache.commons.lang3.concurrent.LazyInitializer;


/**
 * A clone of {@link LazyInitializer}, which doesn't throw any checked exception when get method is invoked. Also, it
 * allows to destroy the created object.
 *
 * @author Alex Objelean
 * @created 24 Oct 2011
 * @since 1.4.2
 */
public abstract class DestroyableLazyInitializer<T> {
  /** Stores the managed object. */
  private volatile T object;

  /**
   * Returns the object wrapped by this instance. On first access the object
   * is created. After that it is cached and can be accessed pretty fast.
   *
   * @return the object initialized by this {@code LazyInitializer}
   * the object
   */
  public T get() {
      // use a temporary variable to reduce the number of reads of the
      // volatile field
      T result = object;

      if (result == null) {
          synchronized (this) {
              result = object;
              if (result == null) {
                  object = result = initialize();
              }
          }
      }

      return result;
  }


  /**
   * Destroy the initialized object. This will trigger the re-initialization when
   * {@link DestroyableLazyInitializer#get()} method is invoked.
   */
  public final void destroy() {
    object = null;
  }

  /**
   * Creates and initializes the object managed by this {@code
   * LazyInitializer}. This method is called by {@link #get()} when the object
   * is accessed for the first time. An implementation can focus on the
   * creation of the object. No synchronization is needed, as this is already
   * handled by {@code get()}.
   *
   * @return the managed data object
   */
  protected abstract T initialize();
}
