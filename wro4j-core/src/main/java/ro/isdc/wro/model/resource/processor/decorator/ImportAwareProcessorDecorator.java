package ro.isdc.wro.model.resource.processor.decorator;

import static org.apache.commons.lang3.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.support.ProcessingType;


/**
 * Check if the decorated processor is considered import aware.
 *
 * @author Alex Objelean
 * @created 16 Oct 2012
 * @since 1.6.0
 */
public class ImportAwareProcessorDecorator
    extends ProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(ImportAwareProcessorDecorator.class);
  private final ProcessingType processingType;
  public ImportAwareProcessorDecorator(final Object processor, final ProcessingType processingType) {
    super(processor);
    notNull(processingType);
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
