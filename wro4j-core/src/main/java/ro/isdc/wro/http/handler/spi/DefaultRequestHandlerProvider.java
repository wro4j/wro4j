package ro.isdc.wro.http.handler.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.http.handler.LazyRequestHandlerDecorator;
import ro.isdc.wro.http.handler.ReloadCacheRequestHandler;
import ro.isdc.wro.http.handler.ReloadModelRequestHandler;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;
import ro.isdc.wro.http.handler.ResourceWatcherRequestHandler;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Provides {@link RequestHandler} implementation from core module.
 *
 * @author Alex Objelena
 * @since 1.5.0
 * @created 23 Sep 2012
 */
public class DefaultRequestHandlerProvider implements RequestHandlerProvider {

  public Map<String, RequestHandler> provideRequestHandlers() {
    final Map<String, RequestHandler> map = new HashMap<String, RequestHandler>();
    map.put(ReloadCacheRequestHandler.ALIAS, new LazyRequestHandlerDecorator(new LazyInitializer<RequestHandler>() {
      @Override
      protected RequestHandler initialize() {
        return new ReloadCacheRequestHandler();
      }
    }));
    map.put(ReloadModelRequestHandler.ALIAS, new LazyRequestHandlerDecorator(new LazyInitializer<RequestHandler>() {
      @Override
      protected RequestHandler initialize() {
        return new ReloadModelRequestHandler();
      }
    }));
    map.put(ResourceProxyRequestHandler.ALIAS, new LazyRequestHandlerDecorator(new LazyInitializer<RequestHandler>() {
      @Override
      protected RequestHandler initialize() {
        return new ResourceProxyRequestHandler();
      }
    }));
    map.put(ResourceWatcherRequestHandler.ALIAS, new LazyRequestHandlerDecorator(new LazyInitializer<RequestHandler>() {
      @Override
      protected RequestHandler initialize() {
        return new ResourceWatcherRequestHandler();
      }
    }));
    return map;
  }
}
