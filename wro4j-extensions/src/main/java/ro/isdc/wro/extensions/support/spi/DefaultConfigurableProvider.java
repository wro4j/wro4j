package ro.isdc.wro.extensions.support.spi;

import java.util.Map;

import ro.isdc.wro.extensions.http.handler.spi.DefaultRequestHandlerProvider;
import ro.isdc.wro.extensions.locator.support.DefaultLocatorProvider;
import ro.isdc.wro.extensions.model.spi.DefaultModelFactoryProvider;
import ro.isdc.wro.extensions.processor.support.DefaultProcessorProvider;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.spi.ModelFactoryProvider;
import ro.isdc.wro.util.Ordered;
import ro.isdc.wro.util.provider.ConfigurableProviderSupport;


/**
 * Default implementation of {@link ConfigurableProviderSupport} which contributes with components from extensions
 * module.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class DefaultConfigurableProvider
    extends ConfigurableProviderSupport
    implements Ordered {
  private final ProcessorProvider processorProvider = new DefaultProcessorProvider();
  private final RequestHandlerProvider requestHandlerProvider = new DefaultRequestHandlerProvider();
  private final ModelFactoryProvider modelFactoryProvider = new DefaultModelFactoryProvider();
  private final LocatorProvider locatorProvider = new DefaultLocatorProvider();

  @Override
  public java.util.Map<String, ResourcePreProcessor> providePreProcessors() {
    return processorProvider.providePreProcessors();
  }

  @Override
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return processorProvider.providePostProcessors();
  }

  @Override
  public Map<String, RequestHandler> provideRequestHandlers() {
    return requestHandlerProvider.provideRequestHandlers();
  }

  @Override
  public Map<String, WroModelFactory> provideModelFactories() {
    return modelFactoryProvider.provideModelFactories();
  }

  @Override
  public Map<String, UriLocator> provideLocators() {
    return locatorProvider.provideLocators();
  }

  @Override
  public int getOrder() {
    // Increase the order to make it precede the providers from the core. This is necessary to use the WebjarsUriLocator
    // before the ServletUriLocator.
    return Ordered.LOWEST + 1;
  }
}
