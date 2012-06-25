package ro.isdc.wro.config;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.Validate;

/**
 * A {@link Callable} decorator responsible for propagating the correlationId to the decorated callable.
 * @author Alex Objelean
 * @created 8 May 2012
 * @since 1.4.6
 */
public class ContextPropagatingCallable<T>
    implements Callable<T> {
  private final String correlationId;
  private final Callable<T> decorated;
  
  public ContextPropagatingCallable(final Callable<T> decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
    this.correlationId = DefaultContext.getCorrelationId();
  }
  
  public T call()
      throws Exception {
    DefaultContext.setCorrelationId(correlationId);
    try {
      return decorated.call();
    } finally {
      DefaultContext.unsetCorrelationId();
    }
  }
}
