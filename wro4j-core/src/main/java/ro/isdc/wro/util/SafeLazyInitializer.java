/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;


/**
 * Implementation of lazy initialization pattern. Same as {@link LazyInitializer}, but doesn't throw any checked
 * exception when get method is invoked.
 *
 * @author Alex Objelean
 * @created 24 Oct 2011
 * @since 1.4.2
 */
public abstract class SafeLazyInitializer<T> {
  private LazyInitializer<T> initializer = new LazyInitializer<T>() {
    @Override
    protected T initialize()
      throws ConcurrentException {
      return SafeLazyInitializer.this.initialize();
    }
  };


  /**
   * Returns the initialized. On first access the object is created. After that it is cached and can
   * be accessed pretty fast.
   *
   * @return the object initialized by this initializer.
   */
  public T get() {
    try {
      return initializer.get();
    } catch (final ConcurrentException e) {
      throw new RuntimeException("Unexpected Exception during initialization", e);
    }
  }


  protected abstract T initialize();
}
