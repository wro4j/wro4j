package ro.isdc.wro.config.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.Callable;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;


/**
 * A {@link Callable} decorator responsible for propagating the correlationId to the decorated callable.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public class ContextPropagatingCallable<T>
    implements Callable<T>, Runnable {
  private final String correlationId;
  private final Callable<T> decorated;

  public ContextPropagatingCallable(final Callable<T> decorated) {
    notNull(decorated);
    this.decorated = decorated;
    this.correlationId = Context.getCorrelationId();
  }

  public static <T> Callable<T> decorate(final Callable<T> callable) {
    notNull(callable);
    return new ContextPropagatingCallable<T>(new Callable<T>() {
      public T call()
          throws Exception {
        return callable.call();
      }
    });
  }

  public static Runnable decorate(final Runnable runnable) {
    notNull(runnable);
    return new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        runnable.run();
        return null;
      }
    });
  }

  public void run() {
    try {
      call();
    } catch (final Exception e) {
      throw WroRuntimeException.wrap(e);
    }
  }

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
