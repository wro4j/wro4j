/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.MinimizeAware;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportedResourceTypeAware;


/**
 * Hides details common to all processors decorators.
 *
 * @author Alex Objelean
 * @created 16 Sep 2011
 * @since 1.4.1
 */
public abstract class AbstractProcessorDecorator
  implements ResourcePreProcessor, ResourcePostProcessor, SupportedResourceTypeAware, MinimizeAware {
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
   * {@inheritDoc}
   */
  public final SupportedResourceType getSupportedResourceType() {
    return ProcessorsUtils.getSupportedResourceType(decoratedProcessor);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isMinimize() {
    return ProcessorsUtils.isMinimizeAwareProcessor(decoratedProcessor);
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
  public final void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
}
