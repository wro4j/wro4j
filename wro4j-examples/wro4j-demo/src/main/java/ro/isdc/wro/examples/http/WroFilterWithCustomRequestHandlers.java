package ro.isdc.wro.examples.http;

import ro.isdc.wro.extensions.http.handler.ModelAsJsonRequestHandler;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.factory.DefaultRequestHandlerFactory;
import ro.isdc.wro.http.handler.factory.RequestHandlerFactory;


/**
 * A filter which uses custom {@link RequestHandler} implementations.
 *
 * @author Alex Objelean
 */
public class WroFilterWithCustomRequestHandlers
    extends WroFilter {
  public WroFilterWithCustomRequestHandlers() {
    final RequestHandlerFactory requestHandlerFactory = new DefaultRequestHandlerFactory()
        .addHandler(new ModelAsJsonRequestHandler());
    setRequestHandlerFactory(requestHandlerFactory);
  }
}
