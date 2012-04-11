package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Decorates a {@link LazyInitializer} which creates a processor.
 * 
 * @author Alex Objelean
 * @since 1.4.6
 */
public class LazyProcessorDecorator
    extends AbstractProcessorDecoratorSupport {
  /**
   * Decorated processor.
   */
  private LazyInitializer<ResourcePreProcessor> processorInitializer;

  public LazyProcessorDecorator(final LazyInitializer<ResourcePreProcessor> preProcessor) {
    this.processorInitializer = preProcessor;
  }
  
  @Override
  protected ResourcePreProcessor getDecoratedProcessor() {
    return processorInitializer.get();
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    processorInitializer.get().process(resource, reader, writer);
  }
}
