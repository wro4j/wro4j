/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import ro.isdc.wro.processor.ResourcePreProcessor;


/**
 * Css preProcessor responsible for handling css @import statement.
 *
 * @author Alex Objelean
 */
public class CssImportPreProcessor
  implements ResourcePreProcessor {
  /** The url pattern */
//'url(' and any number of
  // whitespaces
  private static final Pattern importPattern = Pattern.compile("@import\\s*url\\(\\s*"
    + "[\"']?([^\"']*)[\"']?" // any sequence of characters, except an unescaped ')'
    + "\\s*\\);?", // Any number of whitespaces, then ')'
    Pattern.CASE_INSENSITIVE); // works with 'URL('


  // any sequence of characters, except an unescaped ')'

  /**
   * {@inheritDoc}
   */
  public void process(final String resourceUri, final Reader reader, final Writer writer)
    throws IOException {}

}
