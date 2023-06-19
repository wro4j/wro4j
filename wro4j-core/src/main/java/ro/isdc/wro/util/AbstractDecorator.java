package ro.isdc.wro.util;

import static org.apache.commons.lang3.Validate.notNull;


/**
 * Templated decorator.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public abstract class AbstractDecorator<T>
    implements ObjectDecorator<T> {
  private final T decorated;

  public AbstractDecorator(final T decorated) {
    notNull(decorated);
    this.decorated = decorated;
  }

  /**
   * @return the decorated object.
   */
  @Override
  public final T getDecoratedObject() {
    return decorated;
  }

  /**
   * @return the object which is was originally decorated and is not a decorator itself.
   */
  @Override
  public final T getOriginalDecoratedObject() {
    return getOriginalDecoratedObject(decorated);
  }

  /**
   * @return the object which is was originally decorated and is not a decorator itself.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getOriginalDecoratedObject(final T object) {
    return (object instanceof ObjectDecorator) ? ((ObjectDecorator<T>) object).getOriginalDecoratedObject() : object;
  }

  @Override
  public String toString() {
    return getDecoratedObject().toString();
  }
}
