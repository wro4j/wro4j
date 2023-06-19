package ro.isdc.wro.model.resource.processor.decorator;


import ro.isdc.wro.model.resource.Resource;


/**
 * Enhance the decorated processor with the ability to skip processing based on minimize aware state of the processor.
 * In other words, if the processor is minimize aware and the minimize flag is set to false, the processor won't be
 * applied and the content will remain unchanged.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class MinimizeAwareProcessorDecorator
    extends ProcessorDecorator {
  /**
   * Flag indicating if minimize aware processing is allowed.
   */
  private boolean minimize = true;

  /**
   * Uses minimize flag as true by default.
   *
   * @param processor
   */
  public MinimizeAwareProcessorDecorator(final Object processor) {
    this(processor, true);
  }

  /**
   * Decorates a pre or post processor.
   */
  public MinimizeAwareProcessorDecorator(final Object processor, final boolean minimize) {
    super(processor);
    this.minimize = minimize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isEnabled(final Resource resource) {
    // apply processor only when minimize is required or the processor is not minimize aware
    final boolean applyProcessor = (resource != null && resource.isMinimize() && minimize)
        || (resource == null && minimize) || !isMinimize();
    return super.isEnabled(resource) && applyProcessor;
  }
}
