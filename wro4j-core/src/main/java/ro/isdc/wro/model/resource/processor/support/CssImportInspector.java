package ro.isdc.wro.model.resource.processor.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.util.WroUtil;


/**
 * Encapsulates the matcher creation for css import statements detection. Useful to isolate unit tests. TODO prefer a
 * fluent interface implementation for this class.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public class CssImportInspector {
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssImport"));
  private static final String REGEX_IMPORT_FROM_COMMENTS = WroUtil.loadRegexpWithKey("cssImportFromComments");
  /**
   * The index of the url group in regex.
   */
  private static final int INDEX_URL = 1;
  private final Matcher matcher;

  public CssImportInspector(final String cssContent) {
    matcher = getMatcher(removeImportsFromComments(cssContent));
  }

  /**
   * @return a {@link Matcher} for the processed content using the regexp responsible for identifying css import
   * statements.
   */
  protected Matcher getMatcher(final String cssContent) {
    return PATTERN.matcher(cssContent);
  }

  /**
   * Removes all @import statements from css.
   */
  public final String removeImportStatements() {
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      // replace @import with empty string
      matcher.appendReplacement(sb, StringUtils.EMPTY);
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * @return true if checked css content contains an @import statement.
   */
  public final boolean containsImport() {
    return matcher.find();
  }

  /**
   * @return a list of all resources imported using @import statement.
   */
  public final List<String> findImports() {
    final List<String> list = new ArrayList<String>();
    while (matcher.find()) {
      list.add(extractImportUrl(matcher));
    }
    return list;
  }

  /**
   * Override this method to provide a custom way of extracting the imported resource url.
   *
   * @param matcher
   *          the {@link Matcher} inspecting the parsed css content.
   * @return the url of the imported resources.
   */
  protected String extractImportUrl(final Matcher matcher) {
    return matcher.group(INDEX_URL);
  }

  /**
   * Removes all import statements from provided css content.
   *
   * @return the css content with import statement removed.
   * @VisibleForTesting
   */
  final String removeImportsFromComments(final String content) {
    return content.replaceAll(REGEX_IMPORT_FROM_COMMENTS, StringUtils.EMPTY);
  }
}
