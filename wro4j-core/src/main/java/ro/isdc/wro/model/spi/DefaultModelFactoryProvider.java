package ro.isdc.wro.model.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;


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
  public Map<String, WroModelFactory> provideModelFactories() {
    final Map<String, WroModelFactory> map = new HashMap<String, WroModelFactory>();
    map.put(XmlModelFactory.ALIAS, new XmlModelFactory());
    return map;
  }
}
