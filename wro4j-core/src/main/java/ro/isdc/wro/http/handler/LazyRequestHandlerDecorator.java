package ro.isdc.wro.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * A decorator which allows lazy instantiation of {@link RequestHandler} object.
 *
 * @author Alex Objelean
 * @created 10 Feb 2013
 * @since 1.6.3
 */
public class LazyRequestHandlerDecorator
    extends AbstractDecorator<LazyInitializer<RequestHandler>>
    implements RequestHandler {
  @Inject
  private Injector injector;
  public LazyRequestHandlerDecorator(final LazyInitializer<RequestHandler> initializer) {
    super(initializer);
  }

  /**
   * {@inheritDoc}
   */
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    getDecoratedObject().get().handle(request, response);
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final HttpServletRequest request) {
    return getDecoratedObject().get().accept(request);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return getDecoratedObject().get().isEnabled();
  }
}
