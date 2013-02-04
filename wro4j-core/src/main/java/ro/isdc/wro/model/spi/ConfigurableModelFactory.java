package ro.isdc.wro.model.spi;

import java.util.Map;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * @author Alex Objelean
 * @created 4 Jan 2013
 * @since 1.6.3
 */
public class ConfigurableModelFactory
    extends AbstractConfigurableSingleStrategy<WroModelFactory, ModelFactoryProvider>
    implements WroModelFactory {
  /**
   * Property name to specify alias.
   */
  public static final String KEY = "modelFactory";
  @Inject
  private Injector injector;
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

  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    final WroModelFactory modelFactory = getConfiguredStrategy();
    injector.inject(modelFactory);
    return modelFactory.create();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }
}
