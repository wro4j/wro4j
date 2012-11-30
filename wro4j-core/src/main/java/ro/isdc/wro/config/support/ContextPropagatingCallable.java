package ro.isdc.wro.config.support;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;


/**
 * A {@link Callable} decorator responsible for propagating the correlationId to the decorated callable.
 *
 * @author Alex Objelean
 * @created 8 May 2012
 * @since 1.4.6
 */
public class ContextPropagatingCallable<T>
    implements Callable<T>, Runnable {
  private final String correlationId;
  private final Callable<T> decorated;

  public ContextPropagatingCallable(final Callable<T> decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
    this.correlationId = Context.getCorrelationId();
  }

  public static ContextPropagatingCallable<Void> decorate(final Runnable runnable) {
    Validate.notNull(runnable);
    return new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        runnable.run();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    try {
      call();
    } catch (final Exception e) {
      throw WroRuntimeException.wrap(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public T call()
      throws Exception {
    Context.setCorrelationId(correlationId);
    try {
      return decorated.call();
    } finally {
      Context.unsetCorrelationId();
    }
  }
}
