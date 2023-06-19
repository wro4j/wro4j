/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.decorator;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Default implementation which can decorate a processor. This class is still named {@link ProcessorDecorator}, though
 * it is not abstract (for backward compatibility reasons). It will be renamed to ProcessorDecorator.
 *
 * @author Alex Objelean
 * @since 1.4.1
 */
public class ProcessorDecorator
    extends AbstractProcessorDecoratorSupport<ResourcePreProcessor> {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessorDecorator.class);

  /**
   * Hides the postProcessor adaptation logic. This exist due to differences between pre and post processor interface.
   * This will be removed in 1.5.0 when all processors will have an unified interface.
   */
  public ProcessorDecorator(final Object processor) {
    super(transform(processor));
  }

  private static ResourcePreProcessor transform(final Object processor) {
    notNull(processor);
    if (processor instanceof ResourcePreProcessor) {
      return (ResourcePreProcessor) processor;
    } else if (processor instanceof ResourcePostProcessor) {
      return toPreProcessor((ResourcePostProcessor) processor);
    } else {
      throw new IllegalArgumentException("Invalid processor: " + processor);
    }
  }

  /**
   * Transforms a post processor into pre processor.
   */
  private static ResourcePreProcessor toPreProcessor(final ResourcePostProcessor postProcessor) {
    return new AbstractProcessorDecoratorSupport<ResourcePostProcessor>(postProcessor) {
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

      @Override
      public String toString() {
        return postProcessor.toString();
      }
    };
  }

  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    if (isEnabled(resource)) {
      LOG.debug("Applying processor: {}", toString());
      getDecoratedObject().process(resource, reader, writer);
    } else {
      LOG.debug("Skipping processor: {}", getDecoratedObject());
      IOUtils.copy(reader, writer);
    }
  }

  /**
   * Indicates if the processor is eligible for usage based on provided criteria.
   *
   * @param minimize
   *          - when true the processor should be minimize aware.
   * @param searchedType
   *          - the type of the accepted processor. If the processor will have no type specified it will still be
   *          eligible.
   * @return true if this processor is of searchedType.
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
   * @param resource
   *          {@link ResourcePreProcessor} for which enabled flag should be checked.
   * @return a flag indicating if this processor is enabled. When false, the processing will be skipped and the content
   *         will be left unchanged. This value is true by default.
   */
  protected boolean isEnabled(final Resource resource) {
    return true;
  }

  @Override
  public String toString() {
    return getDecoratedObject().toString();
  }
}
