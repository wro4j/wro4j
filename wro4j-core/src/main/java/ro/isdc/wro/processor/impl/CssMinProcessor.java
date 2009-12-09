/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.processor.algorithm.CSSMin;


/**
 * A processor implementation using {@link CSSMin} algorithm. This processor can be used as both: PreProcessor & postProcessor.
 *
 * @author Alex Objelean
 */
public class CssMinProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * {@inheritDoc}
   */
  public void process(final String resourceUri, final Reader reader, final Writer writer)
    throws IOException {
    process(reader, writer);
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      new CSSMin().formatFile(content, writer);
      writer.flush();
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }
}
