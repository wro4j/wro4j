package ro.isdc.wro.http.handler;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.LazyInitializerDecorator;


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

  private static LazyInitializerDecorator<RequestHandler> decorate(final LazyInitializer<RequestHandler> initializer) {
    return new LazyInitializerDecorator<RequestHandler>(initializer) {
      @Inject
      private Injector injector;
      @Override
      protected RequestHandler initialize() {
        notNull(injector, "This object was not initialized:" + this);
        final RequestHandler handler = super.initialize();
        injector.inject(handler);
        return handler;
      }
    };
  }


  public LazyRequestHandlerDecorator(final LazyInitializer<RequestHandler> initializer) {
    super(decorate(initializer));
  }

  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    getRequestHandler().handle(request, response);
  }

  public boolean accept(final HttpServletRequest request) {
    return getRequestHandler().accept(request);
  }

  public boolean isEnabled() {
    return getRequestHandler().isEnabled();
  }

  /**
   * This method is used to ensure that lazy initialized object is injected as well.
   */
  private RequestHandler getRequestHandler() {
    return getDecoratedObject().get();
  }

}
