/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * A preProcessor, responsible for adding a ';' character to the end of each js file. This ensure that no errors occurs
 * after the merge.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.JS)
public class SemicolonAppenderPreProcessor
  implements ResourceProcessor {
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      writer.write(content);
      // check if the last character is a semicolon and append only if one is missing.
      if (!content.matches("(?is).*;[\\s\\r\\n]*$")) {
        writer.write(';');
      }
    } finally {
      reader.close();
      writer.close();
    }
  }
}
