package ro.isdc.wro.model.spi;

import java.util.Map;

import ro.isdc.wro.model.factory.WroModelFactory;


/**
 * The provider interface for {@link WroModelFactory} implementations.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public interface ModelFactoryProvider {
  /**
   * @return the {@link WroModelFactory} implementations to contribute. The key represents the modelFactories alias.
   */
  Map<String, WroModelFactory> provideModelFactories();
}
