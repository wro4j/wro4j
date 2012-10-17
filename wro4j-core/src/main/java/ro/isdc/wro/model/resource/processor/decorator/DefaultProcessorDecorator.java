package ro.isdc.wro.model.resource.processor.decorator;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.support.ProcessingCriteria;


/**
 * Decorates the processor with a set of common useful decorators during each processing.
 *
 * @author Alex Objelean
 * @created 7 Oct 2012
 * @since 1.5.1
 */
public class DefaultProcessorDecorator
    extends ProcessorDecorator {
  private final ProcessingCriteria criteria;

  public DefaultProcessorDecorator(final Object processor) {
    this(processor, ProcessingCriteria.createDefault());
  }

  public DefaultProcessorDecorator(final Object processor, final ProcessingCriteria criteria) {
    super(decorate(processor, criteria));
    this.criteria = criteria;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    super.process(resource, reader, writer);
  }

  private static ProcessorDecorator decorate(final Object processor, final ProcessingCriteria criteria) {
    notNull(criteria);
    return new ExceptionHandlingProcessorDecorator(new SupportAwareProcessorDecorator(
        new MinimizeAwareProcessorDecorator(new ImportAwareProcessorDecorator(processor, criteria
            .getProcessingType()))));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isEnabled(final Resource resource) {
    final boolean isApplicable = resource != null ? isEligible(criteria.isMinimize(), resource.getType()) : true;
    return super.isEnabled(resource) && isApplicable;
  }
}
