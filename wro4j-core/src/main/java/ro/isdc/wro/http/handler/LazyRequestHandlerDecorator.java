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
  private RequestHandler requestHandler;
  public LazyRequestHandlerDecorator(final LazyInitializer<RequestHandler> initializer) {
    super(initializer);
  }

  /**
   * {@inheritDoc}
   */
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    getRequestHandler().handle(request, response);
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final HttpServletRequest request) {
    return getRequestHandler().accept(request);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return getRequestHandler().isEnabled();
  }

  /**
   * This method is used to ensure that lazy initialized object is injected as well.
   */
  private RequestHandler getRequestHandler() {
    if (requestHandler == null) {
      requestHandler = getDecoratedObject().get();
      injector.inject(requestHandler);
    }
    return requestHandler;
  }

}
