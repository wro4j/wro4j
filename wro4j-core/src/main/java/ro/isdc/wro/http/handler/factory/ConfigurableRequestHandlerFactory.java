/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.http.handler.factory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.model.resource.support.AbstractConfigurableMultipleStrategy;


/**
 * A {@link RequestHandler} implementation which is easy to configure using a {@link Properties} object.
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
public class ConfigurableRequestHandlerFactory
    extends AbstractConfigurableMultipleStrategy<RequestHandler, RequestHandlerProvider>
    implements RequestHandlerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableRequestHandlerFactory.class);
  /**
   * Name of the property used to configure requestHandlers.
   */
  public static final String KEY = "requestHandlers";
  
  private final RequestHandlerFactory requestHandlerFactory = newRequestHandlerFactory();

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getStrategyKey() {
    return KEY;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, RequestHandler> getStrategies(final RequestHandlerProvider provider) {
    return provider.provideRequestHandlers();
  }

  /**
   * {@inheritDoc}
   */
  public Collection<RequestHandler> create() {
    return requestHandlerFactory.create();
  }
  
  /**
   * {@inheritDoc}
   */
  private RequestHandlerFactory newRequestHandlerFactory() {
    final SimpleRequestHandlerFactory factory = new SimpleRequestHandlerFactory();
    final List<RequestHandler> requestHandlers = getConfiguredStrategies();
    for (final RequestHandler requestHandler : requestHandlers) {
      factory.addHandler(requestHandler);
    }
    // use default when none provided
    if (requestHandlers.isEmpty()) {
      LOG.debug("No locators configured. Using Default locator factory.");
      return new DefaultRequestHandlerFactory();
    }
    return factory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<RequestHandlerProvider> getProviderClass() {
    return RequestHandlerProvider.class;
  }
}
