package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.StopWatch;


/**
 * A decorator responsible for tracking the time spent with processing.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public class BenchmarkProcessorDecorator
    extends ProcessorDecorator {
  @Inject
  private ReadOnlyContext context;
  private static final Logger LOG = LoggerFactory.getLogger(BenchmarkProcessorDecorator.class);
  public BenchmarkProcessorDecorator(final Object processor) {
    super(processor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    StopWatch stopWatch = null;
    if (isDebug()) {
      stopWatch = new StopWatch();
      before(stopWatch);
    }
    try {
      super.process(resource, reader, writer);
    } finally {
      if (isDebug()) {
        after(stopWatch);
      }
    }
  }

  /**
   * required to allow processor work even outside of Context cycle.
   */
  private boolean isDebug() {
    return context != null ? context.getConfig().isDebug() : true;
  }

  /**
   * @VisibleForTesting
   */
  void before(final StopWatch stopWatch) {
    stopWatch.start("Using " + this.toString());
  }

  /**
   * @VisibleForTesting
   */
  void after(final StopWatch stopWatch) {
    stopWatch.stop();
    LOG.debug(stopWatch.prettyPrint());
  }
}
