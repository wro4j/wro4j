/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Inspects the resource for copyright (licence) header and inserts them back if the decorated processor removes them.
 * The decorated processor does the compression logic and probably removes copyright comments. If copyright comments are
   * removed, our processor will put them back.
   *
 * @author Alex Objelean
 * @created 14 May 2011
 * @since 1.3.7
 */
public class CopyrightKeeperProcessorDecorator
  extends AbstractProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(CopyrightKeeperProcessorDecorator.class);

  /** The url pattern */
  private static final Pattern PATTERN_COPYRIGHT = Pattern.compile("(?ims)/\\*!.*?\\*/");

  private CopyrightKeeperProcessorDecorator(final ResourceProcessor processor) {
    super(processor);
  }


  public static CopyrightKeeperProcessorDecorator decorate(final ResourceProcessor processor) {
    return new CopyrightKeeperProcessorDecorator(processor);
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      final Matcher originalMatcher = PATTERN_COPYRIGHT.matcher(content);
      final StringBuffer copyrightBuffer = new StringBuffer();
      while (originalMatcher.find()) {
        LOG.debug("found copyright comment");
        // add copyright header to the buffer.
        copyrightBuffer.append(originalMatcher.group());
      }

      LOG.debug("buffer: {}", copyrightBuffer);
      final Writer processedWriter = new StringWriter();
      getDecoratedProcessor().process(resource, new StringReader(content), processedWriter);

      final Matcher processedMatcher = PATTERN_COPYRIGHT.matcher(processedWriter.toString());

      if (!processedMatcher.find()) {
        writer.write(copyrightBuffer.toString());
      }
      writer.write(processedWriter.toString());
    } finally {
      reader.close();
      writer.close();
    }
  }
}
