package ro.isdc.wro.model.resource.processor.decorator;

import static org.apache.commons.lang3.Validate.notNull;

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
  @Inject
  private Injector injector;
  private final Criteria criteria;

  public static class Criteria {
    private ProcessingType processingType = ProcessingType.ALL;
    private boolean minimize = false;

    public ProcessingType getProcessingType() {
      return processingType;
    }

    public Criteria setProcessingType(final ProcessingType processingType) {
      notNull(processingType);
      this.processingType = processingType;
      return this;
    }

    public boolean isMinimize() {
      return minimize;
    }

    public Criteria setMinimize(final boolean minimize) {
      this.minimize = minimize;
      return this;
    }
  }

  public DefaultProcessorDecorator(final Object processor) {
    this(processor, new Criteria());
  }

  public DefaultProcessorDecorator(final Object processor, final Criteria criteria) {
    super(processor);
    notNull(criteria);
    this.criteria = criteria;
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
    // use exceptionHandling as last decorator
    final ResourcePreProcessor decorated = new ExceptionHandlingProcessorDecorator(new SupportAwareProcessorDecorator(
        new MinimizeAwareProcessorDecorator(new ImportAwareProcessorDecorator(getDecoratedObject(), criteria
            .getProcessingType()))));
    injector.inject(decorated);
    return decorated;
  }
}
