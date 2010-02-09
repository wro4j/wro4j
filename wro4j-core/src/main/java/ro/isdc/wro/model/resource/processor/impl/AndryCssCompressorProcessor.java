/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.algorithm.AndryCssCompressor;


/**
 * A processor implementation using {@link AndryCssCompressor} algorithm. This processor can be used as both: PreProcessor & postProcessor.
 *
 * @author Alex Objelean
 */
public class AndryCssCompressorProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final int LINEBREAK_AFTER_CHARACTERS = 8000;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    process(reader, writer);
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    final AndryCssCompressor compressor = new AndryCssCompressor(content);
    compressor.compress(writer, LINEBREAK_AFTER_CHARACTERS);
    writer.flush();
  }
}
