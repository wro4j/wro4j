/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Default implementation of {@link WroCallback} interface with empty implementations.
 *
 * @author Alex Objelean
 * @created 26 Oct 2011
 * @since 1.4.2
 */
public class WroCallbackDecorator
  implements WroCallback {
  private final WroCallback decorated;
  public WroCallbackDecorator(final WroCallback decorated) {
    this.decorated = decorated;
  }
  /**
   * {@inheritDoc}
   */
  public void onBeforeModelCreated() {
    decorated.onBeforeModelCreated();
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {
    decorated.onAfterModelCreated();
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcessingResource(final Resource resource, final Class<ResourcePreProcessor> preProcessorClass) {
    decorated.onBeforePreProcessingResource(resource, preProcessorClass);
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcessingResource(final Resource resource, final Class<ResourcePreProcessor> preProcessorClass) {
    decorated.onAfterPreProcessingResource(resource, preProcessorClass);
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcessing(final Class<ResourcePostProcessor> postProcessorClass) {
    decorated.onBeforePostProcessing(postProcessorClass);
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcessing(final Class<ResourcePostProcessor> postProcessorClass) {
    decorated.onAfterPostProcessing(postProcessorClass);
  }
}
