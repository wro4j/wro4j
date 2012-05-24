package ro.isdc.wro.model.resource.processor.decorator;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


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
