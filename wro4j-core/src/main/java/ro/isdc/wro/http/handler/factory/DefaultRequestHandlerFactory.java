package ro.isdc.wro.http.handler.factory;

import java.util.ArrayList;
import java.util.Collection;

import ro.isdc.wro.http.handler.ReloadCacheRequestHandler;
import ro.isdc.wro.http.handler.ReloadModelRequestHandler;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;


/**
 * Default {@link RequestHandlerFactory} which provides the the following handlers: {@link ReloadCacheRequestHandler} &
 * {@link ReloadModelRequestHandler}.
 * 
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public class DefaultRequestHandlerFactory extends SimpleRequestHandlerFactory {
  /**
   * Creates a factory with a list of default handlers.
   */
  public DefaultRequestHandlerFactory() {
    Collection<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    requestHandlers.add(new ResourceProxyRequestHandler());
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    setHandlers(requestHandlers);
  }
}
