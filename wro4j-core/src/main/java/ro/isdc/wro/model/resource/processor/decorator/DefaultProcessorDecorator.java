package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Decorates the processor with a set of common useful decorators during each processing.
 *
 * @author Alex Objelean
 * @created 7 Oct 2012
 * @since 1.5.1
 */
public class DefaultProcessorDecorator
    extends ProcessorDecorator {
  @Inject
  private Injector injector;
  public DefaultProcessorDecorator(final ResourceProcessor processor) {
    super(processor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    getDecoratedProcessor().process(resource, reader, writer);
  }

  private ResourceProcessor getDecoratedProcessor() {
    final ResourceProcessor decorated = new ExceptionHandlingProcessorDecorator(new SupportAwareProcessorDecorator(
        new MinimizeAwareProcessorDecorator(getDecoratedObject())));
    injector.inject(decorated);
    return decorated;
  }
}
