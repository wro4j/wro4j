package ro.isdc.wro.model.resource.processor.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ro.isdc.wro.util.WroUtil;


/**
 * Encapsulates the matcher creation for css import statements detection. Useful to isolate unit tests. TODO prefer a
 * fluent interface implementation for this class.
 *
 * @author Alex Objelean
 * @created 19 Feb 2013
 * @since 1.6.3
 */
public class CssImportInspector {
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssImport"));
  private static final String REGEX_IMPORT_FROM_COMMENTS = WroUtil.loadRegexpWithKey("cssImportFromComments");
  /**
   * The index of the url group in regex.
   */
  private static final int INDEX_URL = 1;

  /**
   * Removes all @import statements from css.
   */
  public String removeImportStatements(final String content) {
    final Matcher m = PATTERN.matcher(content);
    final StringBuffer sb = new StringBuffer();
    while (m.find()) {
      // replace @import with empty string
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * @return true if checked css content contains an @import statement.
   */
  public boolean containsImport(final String content) {
    return PATTERN.matcher(content).find();
  }

  /**
   * @return a list of all resources imported using @import statement.
   */
  public List<String> findImports(final String content) {
    final List<String> list = new ArrayList<String>();
    final Matcher m = PATTERN.matcher(removeImportsFromComments(content));
    while (m.find()) {
      list.add(m.group(INDEX_URL));
    }
    return list;
  }

  public String removeImportsFromComments(final String content) {
    return content.replaceAll(REGEX_IMPORT_FROM_COMMENTS, "");
  }
}
