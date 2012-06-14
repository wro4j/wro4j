package ro.isdc.wro.model.resource.processor.impl.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;

/**
 * A preProcessor, responsible for removing console.log(..) and console.debug(..) statements.
 *
 * @author Ivar Conradi Østhus
 */
@SupportedResourceType(ResourceType.JS)
public class ConsoleStripperProcessor
  implements ResourcePreProcessor {
  public static final String ALIAS = "consoleStripper";
  /**
   * Matches console statements
   */
  public static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("javascript.consoleStripper"));

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
