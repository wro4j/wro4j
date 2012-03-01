package ro.isdc.wro.model.resource.processor.impl.js;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.util.WroUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * A preProcessor, responsible for removing console.log(..) and console.debug(..) statements.
 *
 * @author Ivar Conradi Østhus
 */
@SupportedResourceType(ResourceType.JS)
public class ConsoleStripperProcessor
  implements ResourcePreProcessor {
  public static final String ALIAS = "consoleStripperProcessor";

  /**
   * Matches console statements
   */
  public static final Pattern PATTERN = Pattern.compile("console.(log|debug|info|count)(\\(.*)\\);");

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer) 
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      String result = ConsoleStripperProcessor.PATTERN.matcher(content).replaceAll("");
      writer.write(result);
    } finally {
      reader.close();
      writer.close();
    }
  }
}
