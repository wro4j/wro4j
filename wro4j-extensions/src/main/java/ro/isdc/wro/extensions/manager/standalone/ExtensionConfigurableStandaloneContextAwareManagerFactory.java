/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.extensions.manager.standalone;

import java.util.Map;

import ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * @author Alex Objelean
 * @created 31 Jul 2011
 * @since 1.4.0
 */
public class ExtensionConfigurableStandaloneContextAwareManagerFactory extends ConfigurableStandaloneContextAwareManagerFactory {
  /**
   * @return a map of preProcessors.
   */
  @Override
  protected Map<String, ResourcePreProcessor> createPreProcessorsMap() {
    final Map<String, ResourcePreProcessor> map = ProcessorsUtils.createPreProcessorsMap();
    ExtensionsConfigurableWroManagerFactory.pupulateMapWithExtensionsProcessors(map);
    return map;
  }

  /**
   * @return a map of postProcessors.
   */
  @Override
  protected Map<String, ResourcePostProcessor> createPostProcessorsMap() {
    final Map<String, ResourcePostProcessor> map = ProcessorsUtils.createPostProcessorsMap();
    ExtensionsConfigurableWroManagerFactory.pupulateMapWithExtensionsProcessors(map);
    return map;
  }
}
