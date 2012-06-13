package ro.isdc.wro.http.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Default {@link RequestHandlerFactory} which provides the the following handlers: {@link ReloadCacheRequestHandler} &
 * {@link ReloadModelRequestHandler}.
 *
 * WroModelAsJsonRequestHandler is added only if the wro4j-extensions library is loaded.
 * 
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public class DefaultRequestHandlerFactory extends SimpleRequestHandlerFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestHandlerFactory.class);

  /**
   * Creates a factory with a list of default handlers.
   */
  public DefaultRequestHandlerFactory() {
    Collection<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    addWroModelAsJsonRequestHandler(requestHandlers);
    setHandlers(requestHandlers);
  }

  private void addWroModelAsJsonRequestHandler(Collection<RequestHandler> requestHandlers) {
    try {
      RequestHandler requestHandler = (RequestHandler) Class.forName(
          "ro.isdc.wro.extensions.http.handler.WroModelAsJsonRequestHandler").newInstance();
      requestHandlers.add(requestHandler);
    } catch (final Exception e) {
      LOG.info("WroModelAsJsonRequestHandler not found and not added as a requestHandler.");
    }
  }
}
