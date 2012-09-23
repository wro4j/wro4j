package ro.isdc.wro.http.handler.factory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Default {@link RequestHandlerFactory} which adds all {@link RequestHandler}'s provided as spi by
 * {@link RequestHandlerProvider} found in classpath.
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
    final List<RequestHandlerProvider> requestHandlerProviders = ProviderFinder.of(RequestHandlerProvider.class).find();
    final List<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    for (RequestHandlerProvider provider : requestHandlerProviders) {
      LOG.debug("using provider: {}", provider);
      requestHandlers.addAll(provider.provideRequestHandlers().values());
    }
    setHandlers(requestHandlers);
  }
}
