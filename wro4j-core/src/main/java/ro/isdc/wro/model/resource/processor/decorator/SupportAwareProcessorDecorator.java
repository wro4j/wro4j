package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;


/**
 * Check if the decorated processor implements {@link SupportAware} interface and throws the {@link WroRuntimeException}
 * with specific message when processor is not supported.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public class SupportAwareProcessorDecorator
    extends ProcessorDecorator {
  public SupportAwareProcessorDecorator(final Object processor) {
    super(processor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final ResourcePreProcessor decoratedProcessor = getDecoratedObject();
    if (decoratedProcessor instanceof SupportAware) {
      if (!((SupportAware) decoratedProcessor).isSupported()) {
        throw new WroRuntimeException(toString() + " processor is not supported on this environment");
      }
    }
    super.process(resource, reader, writer);
  }
}
