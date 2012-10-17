package ro.isdc.wro.model.resource.processor.decorator;

import static org.apache.commons.lang3.Validate.notNull;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
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

  public DefaultProcessorDecorator(final ResourceProcessor processor) {
    this(processor, ProcessingCriteria.createDefault());
  }

  public DefaultProcessorDecorator(final ResourceProcessor processor, final ProcessingCriteria criteria) {
    super(decorate(processor, criteria));
    this.criteria = criteria;
  }

  private static ResourceProcessor decorate(final ResourceProcessor processor, final ProcessingCriteria criteria) {
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
