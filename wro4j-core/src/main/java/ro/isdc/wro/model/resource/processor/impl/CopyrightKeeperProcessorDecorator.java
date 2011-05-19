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
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Inspects the resource for copyright (licence) header and inserts them back if the decorated processor removes them.
 *
 * @author Alex Objelean
 * @created 14 May 2011
 * @since 1.3.7
 */
public class CopyrightKeeperProcessorDecorator
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CopyrightKeeperProcessorDecorator.class);

  /** The url pattern */
  private static final Pattern PATTERN_COPYRIGHT = Pattern.compile("(?ims)/\\*!.*?\\*/");

  /**
   * A processor which does the compression logic and probably removes copyright comments. If copyright comments are
   * removed, our processor will put them back.
   */
  private final ResourcePreProcessor decoratedProcessor;

  private CopyrightKeeperProcessorDecorator(final ResourcePreProcessor preProcessor) {
    this.decoratedProcessor = preProcessor;
  }

  private CopyrightKeeperProcessorDecorator(final ResourcePostProcessor postProcessor) {
    this(ProcessorsUtils.toPreProcessor(postProcessor));
  }

  public static CopyrightKeeperProcessorDecorator decorate(final ResourcePreProcessor preProcessor) {
    return new CopyrightKeeperProcessorDecorator(preProcessor);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
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
      decoratedProcessor.process(resource, new StringReader(content), processedWriter);

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
