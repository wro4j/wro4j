/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Default implementation which can decorate a processor. This class is still named {@link ProcessorDecorator},
 * though it is not abstract (for backward compatibility reasons). It will be renamed to ProcessorDecorator.
 * 
 * @author Alex Objelean
 * @created 16 Sep 2011
 * @since 1.4.1
 */
public class ProcessorDecorator
  extends AbstractProcessorDecoratorSupport {
  /**
   * Decorated processor.
   */
  private final ResourcePreProcessor decoratedProcessor;

  /**
   * Hides the postProcessor adaptation logic. This exist due to differences between pre & post processor interface.
   * This will be removed in 1.5.0 when all processors will have an unified interface.
   */
  public ProcessorDecorator(final Object processor) {
    Validate.notNull(processor);
    if (processor instanceof ResourcePreProcessor) {
      this.decoratedProcessor = (ResourcePreProcessor) processor;
    } else if (processor instanceof ResourcePostProcessor) {
      this.decoratedProcessor = toPreProcessor((ResourcePostProcessor) processor);
    } else throw new IllegalArgumentException("Invalid processor: " + processor); 
  }

  /**
   * Transforms a post processor into pre processor.
   */
  private ResourcePreProcessor toPreProcessor(final ResourcePostProcessor postProcessor) {
    return new AbstractProcessorDecoratorSupport() {
      @Override
      public Object getDecoratedObject() {
        return postProcessor;
      }
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        postProcessor.process(reader, writer);
      }
      @Override
      protected boolean isMinimizeInternal() {
        return isMinimizeForProcessor(postProcessor);
      }
      @Override
      protected SupportedResourceType getSupportedResourceTypeInternal() {
        return getSupportedResourceTypeForProcessor(postProcessor);
      }
    };
  }

  /**
   * @return the decorated processor.
   */
  @Override
  public final ResourcePreProcessor getDecoratedObject() {
    return decoratedProcessor;
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    decoratedProcessor.process(resource, reader, writer);
  }

  /**
   * Indicates if the processor is eligible for usage based on provided criteria.
   * 
   * @param minimize
   *          - when true the processor should be minimize aware.
   * @param searchedType
   *          - the type of the accepted processor. If the processor will have no type specified it will still be
   *          eligible.
   * @return true if the processor is eligible for the following criteria: minimize & type.
   */
  public final boolean isEligible(final boolean minimize, final ResourceType searchedType) {
    Validate.notNull(searchedType);
    
    final SupportedResourceType supportedType = getSupportedResourceType();
    final boolean isTypeSatisfied = supportedType == null
        || (supportedType != null && searchedType == supportedType.value());
    final boolean isMinimizedSatisfied = minimize == true || !isMinimize();
    
    return isTypeSatisfied && isMinimizedSatisfied;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getOriginalDecoratedObject().toString();
  }
}
