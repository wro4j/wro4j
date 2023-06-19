package ro.isdc.wro.http.handler;

import java.io.IOException;

import org.apache.commons.lang3.Validate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.LazyInitializerDecorator;


/**
 * A decorator which allows lazy instantiation of {@link RequestHandler} object.
 *
 * @author Alex Objelean
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
    	Validate.notNull(injector, "This object was not initialized:" + this);
        final RequestHandler handler = super.initialize();
        injector.inject(handler);
        return handler;
      }
    };
  }

  public LazyRequestHandlerDecorator(final LazyInitializer<RequestHandler> initializer) {
    super(decorate(initializer));
  }

  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    getRequestHandler().handle(request, response);
  }

  @Override
  public boolean accept(final HttpServletRequest request) {
    return getRequestHandler().accept(request);
  }

  @Override
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
