package ro.isdc.wro.model.spi;

import java.util.Map;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * @author Alex Objelean
 * @created 4 Jan 2013
 * @since 1.6.3
 */
public class ConfigurableModelFactory
    extends AbstractConfigurableSingleStrategy<WroModelFactory, ModelFactoryProvider>
    implements ModelFactoryProvider {
  /**
   * Property name to specify alias.
   */
  public static final String KEY = "modelFactory";
  /**
   * {@inheritDoc}
   */
  public Map<String, WroModelFactory> provideModelFactories() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroModelFactory getDefaultStrategy() {
    return new XmlModelFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, WroModelFactory> getStrategies(final ModelFactoryProvider provider) {
    return provider.provideModelFactories();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getStrategyKey() {
    return KEY;
  }
}
