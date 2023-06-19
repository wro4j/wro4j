package ro.isdc.wro.model.resource.processor.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ro.isdc.wro.util.WroUtil;


/**
 * Responsible for identifying import statements for <a
 * href="https://github.com/SomMeri/less4j/wiki/Less-Language-Import">LESS language</a>.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public class LessCssImportInspector
    extends CssImportInspector {
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("lessCssImport"));

  public LessCssImportInspector(final String cssContent) {
    super(cssContent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Matcher getMatcher(final String cssContent) {
    return PATTERN.matcher(cssContent);
  }
}
