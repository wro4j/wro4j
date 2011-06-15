/**
 * Copyright wro4j@2010
 */
package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Locates processors to be used for group processing.
 *
 * @author Alex Objelean
 */
public interface ProcessorsFactory {
  /**
   * @return a collection of pre processors to apply.
   */
  Collection<ResourceProcessor> getPreProcessors();
  /**
   * @return a collection of post processors to apply.
   */
  Collection<ResourceProcessor> getPostProcessors();
}
