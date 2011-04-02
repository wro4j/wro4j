/**
 * Copyright wro4j@2010
 */
package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Locates processors to be used for group processing.
 *
 * @author Alex Objelean
 */
public interface ProcessorsFactory {
  /**
   * @return a collection of pre processors to apply.
   */
  Collection<ResourcePreProcessor> getPreProcessors();
  /**
   * @return a collection of post processors to apply.
   */
  Collection<ResourcePostProcessor> getPostProcessors();
}
