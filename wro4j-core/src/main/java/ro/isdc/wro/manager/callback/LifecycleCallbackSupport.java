/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Default implementation of {@link LifecycleCallback} interface with empty implementations.
 * 
 * @author Alex Objelean
 * @created 26 Oct 2011
 * @since 1.4.3
 */
public class LifecycleCallbackSupport
    implements LifecycleCallback {
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
  public void onBeforePreProcess() {
    
  }
  
  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcess() {
    
  }
  
  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcess() {
    
  }
  
  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcess() {
  }
  
}
