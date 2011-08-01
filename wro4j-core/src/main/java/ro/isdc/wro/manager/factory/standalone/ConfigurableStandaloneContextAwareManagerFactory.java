/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.manager.factory.standalone;

import java.util.Map;
import java.util.Properties;

import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;

/**
 * @author Alex Objelean
 * @created 31 Jul 2011
 * @since 1.4.0
 */
public class ConfigurableStandaloneContextAwareManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
  @Override
  protected final ProcessorsFactory newProcessorsFactory() {
    return new ConfigurableProcessorsFactory().setProperties(createProperties()).setPreProcessorsMap(
      createPreProcessorsMap()).setPostProcessorsMap(createPostProcessorsMap());
  }

  /**
   * @return {@link Properties} from where processors lookup will be performed. By default an empty {@link Properties} object is returned.
   */
  protected Properties createProperties() {
    return new Properties();
  }

  /**
   * @return a map of preProcessors.
   */
  protected Map<String, ResourcePreProcessor> createPreProcessorsMap() {
    return ProcessorsUtils.createPreProcessorsMap();
  }

  /**
   * @return a map of postProcessors.
   */
  protected Map<String, ResourcePostProcessor> createPostProcessorsMap() {
    return ProcessorsUtils.createPostProcessorsMap();
  }
}
