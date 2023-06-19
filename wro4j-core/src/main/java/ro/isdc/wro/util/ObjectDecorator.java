package ro.isdc.wro.util;

/**
 * Describe a class which acts as a decorator. This class is used by injector to perform recursive injection. 
 * 
 * @author Alex Objelean
 * @since 1.4.6
 */
public interface ObjectDecorator<T> {
  /**
   * @return the decorated object.
   */
  T getDecoratedObject();
  
  /**
   * @return the last non decorator object from decorators chain.
   */
  T getOriginalDecoratedObject();
}
