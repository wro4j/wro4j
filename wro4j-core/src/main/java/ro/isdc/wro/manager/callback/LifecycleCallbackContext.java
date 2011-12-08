/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Contains properties accessible by a callback during execution life-cycle.
 * 
 * @author Alex Objelean
 * @created 8 Dec 2011
 * @since 1.4.3
 */
public class LifecycleCallbackContext {
  /**
   * The currently processed resource.
   */
  private Resource resource;
  /**
   * The preProcessor being used.
   */
  private ResourcePreProcessor preProcessor;
  /**
   * The postProcessor being used.
   */
  private ResourcePostProcessor postProcessor;
  
  public Resource getResource() {
    return resource;
  }
  
  public void setResource(final Resource resource) {
    this.resource = resource;
  }
  
  public ResourcePreProcessor getPreProcessor() {
    return preProcessor;
  }
  
  public void setPreProcessor(final ResourcePreProcessor preProcessor) {
    this.preProcessor = preProcessor;
  }
  
  public ResourcePostProcessor getPostProcessor() {
    return postProcessor;
  }
  
  public void setPostProcessor(final ResourcePostProcessor postProcessor) {
    this.postProcessor = postProcessor;
  }
}
