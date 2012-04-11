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
 * @created 11 Apr 2012
 * @since 1.4.6
 */
public abstract class AbstractProcessorDecoratorSupport
  implements ResourcePreProcessor, ResourcePostProcessor, SupportedResourceTypeAware, MinimizeAware {
  /**
   * {@inheritDoc}
   */
  public final SupportedResourceType getSupportedResourceType() {
    return ProcessorsUtils.getSupportedResourceType(getDecoratedProcessor());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isMinimize() {
    return ProcessorsUtils.isMinimizeAwareProcessor(getDecoratedProcessor());
  }

  /**
   * @return the decorated processor.
   */
  protected abstract ResourcePreProcessor getDecoratedProcessor();

  /**
   * {@inheritDoc}
   */
  public final void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
}
