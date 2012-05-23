package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Enhance the decorated processor with the ability to skip processing based on minimize aware state of the processor.
 * In other words, if the processor is minimize aware and the minimize flag is set to false, the processor won't be
 * applied and the content will remain unchanged.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 * @created 20 May 2012
 */
public class MinimizeAwareProcessorDecorator
    extends ProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(MinimizeAwareProcessorDecorator.class);
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
   * The decorated processor will skip processing if the processor has @Minimize annotation and resource being processed
   * doesn't require the minimization.
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final ResourcePreProcessor processor = getDecoratedObject();
    // apply processor only when minimize is required or the processor is not minimize aware
    final boolean applyProcessor = (resource != null && resource.isMinimize() && minimize)
        || (resource == null && minimize) || !isMinimize();
    if (applyProcessor) {
      LOG.debug("Using Processor: {}", processor);
      processor.process(resource, reader, writer);
    } else {
      LOG.debug("Skipping processor: {}", processor);
      IOUtils.copy(reader, writer);
    }
  }
}
