/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Defines callbacks invoked by the wro4j during processing.
 *
 * @author Alex Objelean
 * @created 26 Oct 2011
 * @since 1.4.2
 */
public interface WroCallback {
  /**
   * Invoked before starting model creation.
   */
  void onBeforeModelCreated();

  /**
   * Invoked after the model is created.
   */
  void onAfterModelCreated();

  void onBeforePreProcessingResource(final Resource resource, final Class<ResourcePreProcessor> preProcessorClass);


  void onAfterPreProcessingResource(final Resource resource, final Class<ResourcePreProcessor> preProcessorClass);


  void onBeforePostProcessing(final Class<ResourcePostProcessor> postProcessorClass);


  void onAfterPostProcessing(final Class<ResourcePostProcessor> postProcessorClass);
}
