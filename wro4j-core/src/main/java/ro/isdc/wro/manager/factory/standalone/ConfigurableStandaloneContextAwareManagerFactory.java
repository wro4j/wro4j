/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.manager.factory.standalone;

import java.util.Properties;

import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;

/**
 * @author Alex Objelean
 * @since 1.4.0
 */
public class ConfigurableStandaloneContextAwareManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
  @Override
  protected final ProcessorsFactory newProcessorsFactory() {
    return new ConfigurableProcessorsFactory().setProperties(createProperties());
  }

  /**
   * @return {@link Properties} from where processors lookup will be performed. By default an empty {@link Properties}
   *         object is returned.
   */
  protected Properties createProperties() {
    return new Properties();
  }
}
