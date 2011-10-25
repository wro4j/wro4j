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
public class WroCallbackSupport
  implements WroCallback {
  /**
   * {@inheritDoc}
   */
  public void onBeforeModelCreated() {

  }

  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {

  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcessingResource(final Resource resource, final Class<ResourcePreProcessor> preProcessorClass) {}

  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcessingResource(final Resource resource, final Class<ResourcePreProcessor> preProcessorClass) {}

  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcessing(final Class<ResourcePostProcessor> postProcessorClass) {}

  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcessing(final Class<ResourcePostProcessor> postProcessorClass) {}
}
