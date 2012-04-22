package ro.isdc.wro.model.resource.processor.impl;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;


/**
 * This class exist only for backward compatibility (third party implementation and documentation) and will be removed
 * in 1.5.0.
 * 
 * @author Alex Objelean
 * @deprecated use {@link ProcessorDecorator} instead.
 */
@Deprecated
public abstract class AbstractProcessorDecorator
    extends ProcessorDecorator {

  public AbstractProcessorDecorator(ResourcePreProcessor preProcessor) {
    super(preProcessor);
  }
  
  public AbstractProcessorDecorator(ResourcePostProcessor postProcessor) {
    super(postProcessor);
  }
}
