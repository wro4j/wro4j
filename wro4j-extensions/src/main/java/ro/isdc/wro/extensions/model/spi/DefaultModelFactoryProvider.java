package ro.isdc.wro.extensions.model.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.extensions.model.factory.GroovyModelFactory;
import ro.isdc.wro.extensions.model.factory.JsonModelFactory;
import ro.isdc.wro.extensions.model.factory.SmartWroModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.spi.ModelFactoryProvider;


/**
 * Default provider of {@link WroModelFactory} for the core module.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public class DefaultModelFactoryProvider
    implements ModelFactoryProvider {

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, WroModelFactory> provideModelFactories() {
    final Map<String, WroModelFactory> map = new HashMap<String, WroModelFactory>();
    map.put(GroovyModelFactory.ALIAS, new GroovyModelFactory());
    map.put(JsonModelFactory.ALIAS, new JsonModelFactory());
    map.put(SmartWroModelFactory.ALIAS, new SmartWroModelFactory());
    return map;
  }
}
