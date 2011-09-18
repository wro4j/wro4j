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
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public class VariablizeColorsCssProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  public static final String ALIAS = "variablizeColors";

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String result = new Lessify().variablizeColors(IOUtils.toString(reader));
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
