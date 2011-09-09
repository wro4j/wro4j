/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.Lessify;


/**
 * A processor which transform all colors to #rgb format.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public class ConformColorsCssProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  public static final String ALIAS = "conformColors";

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String result = new Lessify().conformColors(IOUtils.toString(reader));
      writer.write(result);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
}
