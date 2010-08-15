/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.algorithm.Lessify;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * @author Alex Objelean
 */
public class VariablizeColorsCssProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private Lessify lessify = new Lessify();
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String result = lessify.variablizeColors(IOUtils.toString(reader));
      writer.write(result);
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    process(reader, writer);
  }

}
