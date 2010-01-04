/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.annot.SupportedResourceType;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.processor.algorithm.JawrCssMinifier;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;


/**
 * A processor implementation using {@link JawrCssMinifier} algorithm. This processor can be used as both: PreProcessor & postProcessor.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(type=ResourceType.CSS)
public class JawrCssMinifierProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
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
    try {
      final String content = IOUtils.toString(reader);
      final StringBuffer result = new JawrCssMinifier().minifyCSS(new StringBuffer(content));
      writer.write(result.toString());
      writer.flush();
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }
}
