package ro.isdc.wro.util.provider;

import ro.isdc.wro.cache.spi.CacheStrategyProvider;
import ro.isdc.wro.http.handler.spi.RequestHandlerProvider;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.HashStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;
import ro.isdc.wro.model.spi.ModelFactoryProvider;

/**
 * Bring all providers in a single place. Simplifies the way providers are implemented.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public interface ConfigurableProvider
    extends ProcessorProvider, NamingStrategyProvider, HashStrategyProvider, LocatorProvider, CacheStrategyProvider,
    RequestHandlerProvider, ModelFactoryProvider {
}
