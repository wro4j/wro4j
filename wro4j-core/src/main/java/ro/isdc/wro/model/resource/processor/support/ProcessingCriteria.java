package ro.isdc.wro.model.resource.processor.support;

import static org.apache.commons.lang3.Validate.notNull;


/**
 * The criteria used to apply or skip the processor.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public final class ProcessingCriteria {
  private ProcessingType processingType = ProcessingType.ALL;
  private boolean minimize = false;

  private ProcessingCriteria() {
  }

  public ProcessingType getProcessingType() {
    return processingType;
  }

  public static ProcessingCriteria create(final ProcessingType processingType, final boolean minimize) {
    return new ProcessingCriteria().setProcessingType(processingType).setMinimize(minimize);
  }

  public static ProcessingCriteria createDefault(final boolean minimize) {
    return new ProcessingCriteria().setMinimize(minimize);
  }

  public ProcessingCriteria setProcessingType(final ProcessingType processingType) {
    notNull(processingType);
    this.processingType = processingType;
    return this;
  }

  public boolean isMinimize() {
    return minimize;
  }

  public ProcessingCriteria setMinimize(final boolean minimize) {
    this.minimize = minimize;
    return this;
  }
}