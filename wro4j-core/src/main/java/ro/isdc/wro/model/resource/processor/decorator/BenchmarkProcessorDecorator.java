package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.StopWatch;



/**
 * A decorator responsible for tracking the time spent with processing.
 *
 * @author Alex Objelean
 * @since 1.6.3
 * @created 12 Jan 2013
 */
public class BenchmarkProcessorDecorator
    extends ProcessorDecorator {
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
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("Using " + this.toString());
    try {
      super.process(resource, reader, writer);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
