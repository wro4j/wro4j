package ro.isdc.wro.examples.http;

import ro.isdc.wro.extensions.http.handler.ExposeModelRequestHandler;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.http.handler.DefaultRequestHandlerFactory;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.RequestHandlerFactory;


/**
 * A filter which uses custom {@link RequestHandler} implementations.
 * 
 * @author Alex Objelean
 */
public class WroFilterWithCustomRequestHandlers
    extends WroFilter {
  public WroFilterWithCustomRequestHandlers() {
    RequestHandlerFactory requestHandlerFactory = new DefaultRequestHandlerFactory().addHandler(new ExposeModelRequestHandler());
    setRequestHandlerFactory(requestHandlerFactory);
  }
}
