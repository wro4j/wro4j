package ro.isdc.wro.model.resource.processor.support;

import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * All implementation of this interface will contribute to the list of available processors discovered during
 * application initialization.
 * 
 * @author Alex Objelean
 */
public interface ProcessorProvider {
  /**
   * @return the preProcessors to contribute. The key represents the processor alias.
   */
  Map<String, ResourcePreProcessor> providePreProcessors();
  
  /**
   * @return the postProcessors to contribute. The key represents the processor alias.
   */
  Map<String, ResourcePostProcessor> providePostProcessors();
}
