/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.processor.ResourcePreProcessor;


/**
 * Css preProcessor responsible for handling css @import statement.
 *
 * @author Alex Objelean
 */
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
  public void process(final String resourceUri, final Reader reader, final Writer writer)
    throws IOException {
    final String css = IOUtils.toString(reader);
    final String result = parseCss(css);
    writer.write(result);
    writer.close();
  }

  /**
   * Parse css, find all defined variables & replace them.
   *
   * @param css to parse.
   */
  private String parseCss(final String css) {
    //map containing variables & their values
    final Map<String, String> map = new HashMap<String, String>();
    final StringBuffer sb = new StringBuffer();
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final String variablesBody = m.group(1);
      LOG.debug("import statement: " + m.group(0));
      LOG.debug("import url: " + variablesBody);
      //extract variables
      //map.putAll(extractVariables(variablesBody));
      //remove variables definition

      m.appendReplacement(sb, "1");
    }
    m.appendTail(sb);

    //final String result = replaceVariables(sb.toString(), map);
    //LOG.debug("replaced variables: " + result);
//    return result;
    return sb.toString();
  }

}
