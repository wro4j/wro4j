package ro.isdc.wro.extensions.http.handler.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.extensions.http.handler.ModelAsJsonRequestHandler;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;


/**
 * Provides {@link RequestHandler} implementation from core module.
 * 
 * @author Alex Objelena
 * @since 1.5.0
 * @created 23 Sep 2012
 */
public class DefaultRequestHandlerProvider implements RequestHandlerProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, RequestHandler> provideRequestHandlers() {
    final Map<String, RequestHandler> map = new HashMap<String, RequestHandler>();
    map.put(ModelAsJsonRequestHandler.ALIAS, new ModelAsJsonRequestHandler());
    return map;
  }
}
