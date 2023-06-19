/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;



/**
 * A clone of {@link LazyInitializer}, which doesn't throw any checked exception when get method is invoked.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public abstract class LazyInitializer<T> {
  /** Stores the managed object. */
  volatile T object;

  /**
   * Returns the object wrapped by this instance. On first access the object is created. After that it is cached and can
   * be accessed pretty fast.
   *
   * @return the object initialized by this {@code LazyInitializer} the object
   */
  public final T get() {
    // use a temporary variable to reduce the number of reads of the volatile field
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
   * @return true if the object was already initialized.
   */
  public final boolean isInitialized() {
    return object != null;
  }

  /**
   * Creates and initializes the object managed by this {@code LazyInitializer}. This method is called by {@link #get()}
   * when the object is accessed for the first time. An implementation can focus on the creation of the object. No
   * synchronization is needed, as this is already handled by {@code get()}.
   *
   * @return the managed data object
   */
  protected abstract T initialize();
}
