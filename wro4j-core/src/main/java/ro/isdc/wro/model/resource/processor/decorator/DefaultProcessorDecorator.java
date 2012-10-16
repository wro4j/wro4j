package ro.isdc.wro.model.resource.processor.decorator;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.ProcessingType;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Decorates the processor with a set of common useful decorators during each processing.
 *
 * @author Alex Objelean
 * @created 7 Oct 2012
 * @since 1.5.1
 */
public class DefaultProcessorDecorator
    extends ProcessorDecorator {
  private final ProcessingType processingType;
  @Inject
  private Injector injector;

  public DefaultProcessorDecorator(final Object processor) {
    this(processor, ProcessingType.ALL);
  }

  public DefaultProcessorDecorator(final Object processor, final ProcessingType processingType) {
    super(processor);
    assertNotNull(processingType);
    this.processingType = processingType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    getDecoratedProcessor().process(resource, reader, writer);
  }

  private ResourcePreProcessor getDecoratedProcessor() {
    ProcessorDecorator decoratedProcessor = new SupportAwareProcessorDecorator(new MinimizeAwareProcessorDecorator(
        getDecoratedObject()));
    decoratedProcessor = new ImportAwareProcessorDecorator(decoratedProcessor) {
      @Override
      public boolean isImportAware() {
        return super.isImportAware() && processingType == ProcessingType.IMPORT_ONLY;
      }
    };
    //use exceptionHandling as last decorator
    final ResourcePreProcessor decorated = new ExceptionHandlingProcessorDecorator(decoratedProcessor);
    injector.inject(decorated);
    return decorated;
  }
}
