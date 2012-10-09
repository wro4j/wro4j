package ro.isdc.wro.examples.support.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


public class CustomProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CustomProcessor.class);
  public static String ALIAS = "customProcessor";
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    LOG.info("Applying {}", ALIAS);
    IOUtils.copy(reader, writer);
  }
}
