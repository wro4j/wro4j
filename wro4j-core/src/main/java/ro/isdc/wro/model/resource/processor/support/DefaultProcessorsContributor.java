package ro.isdc.wro.model.resource.processor.support;

import java.util.Map;

import ro.isdc.wro.model.resource.processor.ProcessorsContributor;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * The implementation which contributes with processors from core module.
 * 
 * @author Alex Objelean
 * @created 1 Jun 2012
 */
public class DefaultProcessorsContributor
    implements ProcessorsContributor {
  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePreProcessor> contributePreProcessors() {
    return ProcessorsUtils.createPreProcessorsMap();
  }
  
  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePostProcessor> contributePostProcessors() {
    return ProcessorsUtils.createPostProcessorsMap();
  }
}
