package ro.isdc.wro.http.handler.factory;

import java.util.ArrayList;
import java.util.Collections;
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
 * @since 1.4.7
 */
public class DefaultRequestHandlerFactory
    extends SimpleRequestHandlerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestHandlerFactory.class);

  /**
   * Creates a factory with a list of default handlers.
   */
  public DefaultRequestHandlerFactory() {
    List<RequestHandlerProvider> requestHandlerProviders = Collections.emptyList();
    try {
      requestHandlerProviders = ProviderFinder.of(RequestHandlerProvider.class).find();
    } catch (final NoClassDefFoundError e) {
    }
    final List<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    for (final RequestHandlerProvider provider : requestHandlerProviders) {
      LOG.debug("using provider: {}", provider);
      requestHandlers.addAll(provider.provideRequestHandlers().values());
    }
    setHandlers(requestHandlers);
  }
}
