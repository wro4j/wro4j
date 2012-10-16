package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;


/**
 * Check if the decorated processor is considered import aware.
 *
 * @author Alex Objelean
 * @created 16 Oct 2012
 * @since 1.5.1
 */
public class ImportAwareProcessorDecorator
    extends ProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(ImportAwareProcessorDecorator.class);
  public ImportAwareProcessorDecorator(final Object processor) {
    super(processor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final ProcessorDecorator decorator = new ProcessorDecorator(getDecoratedObject());
    if (decorator.isImportAware()) {
      super.process(resource, reader, writer);
    } else {
      LOG.debug("Skipping processor: {}", getDecoratedObject());
      IOUtils.copy(reader, writer);
    }
  }
}
