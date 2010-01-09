/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.annot.SupportedResourceType;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;


/**
 * Css preProcessor responsible for handling css @import statement.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(type=ResourceType.CSS)
public class CssImportPreProcessor
  implements ResourcePreProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CssImportPreProcessor.class);

  /** The url pattern */
  private static final Pattern PATTERN = Pattern.compile("@import\\s*url\\(\\s*"
    + "[\"']?([^\"']*)[\"']?" // any sequence of characters, except an unescaped ')'
    + "\\s*\\);?", // Any number of whitespaces, then ')'
    Pattern.CASE_INSENSITIVE); // works with 'URL('

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String result = parseCss(resource, reader);
    writer.write(result);
    writer.close();
  }

  /**
   * Parse css, find all import statements.
   *
   * @param resource {@link Resource} where the parsed css resides.
   */
  private String parseCss(final Resource resource, final Reader reader) throws IOException {
    final String css = IOUtils.toString(reader);
    final Stack<Resource> stack = new Stack<Resource>();
    final StringBuffer sb = new StringBuffer();
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final String variablesBody = m.group(1);
      LOG.debug("import statement: " + m.group(0));
      LOG.debug("import url: " + variablesBody);
      stack.push(resource);
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    processResource(resource, stack);
    System.out.println(sb.toString());
    System.out.println(stack);
    return sb.toString();
  }

  /**
   * @param resource
   * @param stack
   */
  private void processResource(final Resource resource, final Stack<Resource> stack) {
    while(stack.isEmpty()) {
      final Resource item = stack.pop();
    }
  }

}
