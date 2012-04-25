package ro.isdc.wro.util;

import org.apache.commons.lang3.Validate;


/**
 * Templated decorator.
 * 
 * @author Alex Objelean
 * @created 25 Apr 2012
 * @since 1.4.6
 */
public abstract class AbstractDecorator<T> {
  private T decorated;
  
  public AbstractDecorator(final T decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }
  
  /**
   * @return the decorated object.
   */
  public final T getDecoratedObject() {
    return decorated;
  }
}
