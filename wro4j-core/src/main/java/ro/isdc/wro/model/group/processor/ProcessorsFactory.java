/**
 * Copyright wro4j@2010
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Builds processors to be used for group processing.
 *
 * @author Alex Objelean
 */
public interface ProcessorsFactory {
  Collection<ResourcePreProcessor> getResourcePreProcessors();
  Collection<ResourcePostProcessor> getResourcePostProcessors();
}
