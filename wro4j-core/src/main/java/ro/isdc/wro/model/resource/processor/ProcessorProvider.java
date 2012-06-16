package ro.isdc.wro.model.resource.processor;

import java.util.Map;



/**
 * All implementation of this interface will contribute to the list of available processors discovered during
 * application initialization.
 * 
 * @author Alex Objelean
 * @created 1 Jun 2012
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
