/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * A preProcessor, responsible for adding a ';' character to the end of each js file. This ensure that no errors occurs
 * after the merge.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.JS)
public class SemicolonAppenderPreProcessor
  implements ResourcePreProcessor {
  public static final String ALIAS = "semicolonAppender";
  /**
   * check if the last character is a semicolon and append only if one is missing.
   */
  private static final String PATTERN = WroUtil.loadRegexpWithKey("javascript.hasSemicolon");

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String script = IOUtils.toString(reader);
      writer.write(script);
      if (isSemicolonNeeded(script)) {
        writer.write(';');
      }
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * @param script script to process.
   * @return true if the processed content requires semicolon.
   */
  private boolean isSemicolonNeeded(final String script) {
    return !(script.matches(PATTERN) || StringUtils.isEmpty(script));
  }
}
