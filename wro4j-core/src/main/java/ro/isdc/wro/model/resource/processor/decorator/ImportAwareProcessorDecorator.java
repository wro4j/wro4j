package ro.isdc.wro.model.resource.processor.decorator;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.support.ProcessingType;


/**
 * Check if the decorated processor is considered import aware.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public class ImportAwareProcessorDecorator
    extends ProcessorDecorator {

  private final ProcessingType processingType;
  public ImportAwareProcessorDecorator(final Object processor, final ProcessingType processingType) {
    super(processor);
    Validate.notNull(processingType);
    this.processingType = processingType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isEnabled(final Resource resource) {
    return super.isEnabled(resource) && isApplicable();
  }

  /**
   * @return true if processor can be applied based on configured {@link ProcessingType}
   */
  private boolean isApplicable() {
    return ProcessingType.ALL == processingType || isImportAware()
        && processingType == ProcessingType.IMPORT_ONLY;
  }
}
