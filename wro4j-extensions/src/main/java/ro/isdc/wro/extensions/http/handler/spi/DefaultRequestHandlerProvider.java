package ro.isdc.wro.extensions.http.handler.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.extensions.http.handler.ModelAsJsonRequestHandler;
import ro.isdc.wro.http.handler.LazyRequestHandlerDecorator;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Provides {@link RequestHandler} implementation from core module.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class DefaultRequestHandlerProvider implements RequestHandlerProvider {
  @Override
  public Map<String, RequestHandler> provideRequestHandlers() {
    final Map<String, RequestHandler> map = new HashMap<String, RequestHandler>();
    map.put(ModelAsJsonRequestHandler.ALIAS, new LazyRequestHandlerDecorator(new LazyInitializer<RequestHandler>() {
      @Override
      protected RequestHandler initialize() {
        return new ModelAsJsonRequestHandler();
      }
    }));
    return map;
  }
}
