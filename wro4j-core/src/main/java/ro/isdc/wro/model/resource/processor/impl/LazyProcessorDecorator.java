package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.MinimizeAware;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportedResourceTypeAware;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Decorates a {@link LazyInitializer} which creates a processor.
 * 
 * @author Alex Objelean
 * @since 1.4.6
 */
public class LazyProcessorDecorator
implements ResourcePreProcessor, ResourcePostProcessor, SupportedResourceTypeAware, MinimizeAware {
  /**
   * Decorated processor.
   */
  private LazyInitializer<ResourcePreProcessor> decoratedProcessor;

  public LazyProcessorDecorator(final LazyInitializer<ResourcePreProcessor> preProcessor) {
    this.decoratedProcessor = preProcessor;
  }

  /**
   * {@inheritDoc}
   */
  public final SupportedResourceType getSupportedResourceType() {
    return ProcessorsUtils.getSupportedResourceType(decoratedProcessor.get());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isMinimize() {
    return ProcessorsUtils.isMinimizeAwareProcessor(decoratedProcessor.get());
  }

  /**
   * {@inheritDoc}
   */
  public final void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(Resource resource, Reader reader, Writer writer)
      throws IOException {
    decoratedProcessor.get().process(resource, reader, writer);
  }
}
