/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.impl;

import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Hides details common to all processors decorators.
 *
 * @author Alex Objelean
 * @created 16 Sep 2011
 * @since 1.4.1
 */
public abstract class AbstractProcessorDecorator
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
}
