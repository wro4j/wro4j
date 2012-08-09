package ro.isdc.wro.http.handler.factory;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.ReloadCacheRequestHandler;
import ro.isdc.wro.http.handler.ReloadModelRequestHandler;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;


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
    // TODO use provider to load all available handlers as default behavior.
    Collection<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    requestHandlers.add(new ResourceProxyRequestHandler());
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    addExtensionsHandlers(requestHandlers);
    setHandlers(requestHandlers);
  }

  /**
   * Add more handlers via reflection. If the module containing this handler is not available, it won't be added.
   */
  private void addExtensionsHandlers(final Collection<RequestHandler> requestHandlers) {
    try {
      // TODO add a test in extension module which proves that this handler is added when the the extensions module is
      // available.
      RequestHandler requestHandler = (RequestHandler) Class.forName(
          "ro.isdc.wro.extensions.http.handler.ModelAsJsonRequestHandler").newInstance();
      requestHandlers.add(requestHandler);
    } catch (final Exception e) {
      LOG.debug("ModelAsJsonRequestHandler not found, probably because wro4j-extensions "
          + "module is missing from the classpath.");
    }
  }
}
