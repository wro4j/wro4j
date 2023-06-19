package ro.isdc.wro.model.factory;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;
import ro.isdc.wro.model.spi.ModelFactoryProvider;


/**
 * @author Alex Objelean
 * @since 1.6.3
 */
public class ConfigurableModelFactory
    extends AbstractConfigurableSingleStrategy<WroModelFactory, ModelFactoryProvider>
    implements WroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableModelFactory.class);
  /**
   * Property name to specify alias.
   */
  public static final String KEY = "modelFactory";

  @Override
  protected WroModelFactory getDefaultStrategy() {
    try {
      LOG.debug("Trying to use SmartWroModelFactory as default model factory");
      final Class<? extends WroModelFactory> smartFactoryClass = Class.forName(
          "ro.isdc.wro.extensions.model.factory.SmartWroModelFactory").asSubclass(WroModelFactory.class);
      return smartFactoryClass.getDeclaredConstructor().newInstance();
    } catch (final Exception e) {
      LOG.debug("SmartWroModelFactory is not available. Using default model factory.");
      LOG.debug("Reason: {}", e.getMessage());
    }
    return new XmlModelFactory();
  }

  @Override
  protected Map<String, WroModelFactory> getStrategies(final ModelFactoryProvider provider) {
    return provider.provideModelFactories();
  }

  @Override
  protected Class<ModelFactoryProvider> getProviderClass() {
    return ModelFactoryProvider.class;
  }

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
  public WroModel create() {
    return getConfiguredStrategy().create();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getConfiguredStrategy().destroy();
  }
}
