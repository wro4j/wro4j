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
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.SupportedResourceTypeAware;


/**
 * Hides details common to all processors decorators.
 *
 * @author Alex Objelean
 * @created 16 Sep 2011
 * @since 1.4.1
 */
public abstract class AbstractProcessorDecorator
  implements ResourceProcessor, SupportedResourceTypeAware, MinimizeAware {
  /**
   * Decorated processor.
   */
  private final ResourceProcessor decoratedProcessor;

  public AbstractProcessorDecorator(final ResourceProcessor preProcessor) {
    this.decoratedProcessor = preProcessor;
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
  public final ResourceProcessor getDecoratedProcessor() {
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
