/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Default implementation which can decorate a processor. This class is still named {@link AbstractProcessorDecorator},
 * though it is not abstract (for backward compatibility reasons). It will be renamed to ProcessorDecorator.
 * 
 * @author Alex Objelean
 * @created 16 Sep 2011
 * @since 1.4.1
 */
public class AbstractProcessorDecorator
  extends AbstractProcessorDecoratorSupport {
  /**
   * Decorated processor.
   */
  private final ResourcePreProcessor decoratedProcessor;

  public AbstractProcessorDecorator(final ResourcePreProcessor preProcessor) {
    this.decoratedProcessor = preProcessor;
  }

  public AbstractProcessorDecorator(final ResourcePostProcessor postProcessor) {
    this(ProcessorsUtils.toPreProcessor(postProcessor));
  }

  /**
   * @return the decorated processor.
   */
  public final ResourcePreProcessor getDecoratedProcessor() {
    return decoratedProcessor;
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    decoratedProcessor.process(resource, reader, writer);
  }
}
