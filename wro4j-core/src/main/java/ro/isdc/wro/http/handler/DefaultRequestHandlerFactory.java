package ro.isdc.wro.http.handler;

import java.util.ArrayList;
import java.util.Collection;


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
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    setHandlers(requestHandlers);
  }
}
