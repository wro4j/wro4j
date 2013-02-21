package ro.isdc.wro.model.resource.processor.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ro.isdc.wro.util.WroUtil;


/**
 * Encapsulates the matcher creation for css backround url's detection. Useful to isolate unit tests.
 *
 * @author Alex Objelean
 * @created 20 Feb 2013
 * @since 1.6.3
 */
public class FallbackCssDataUriUrlInspector extends CssUrlInspector {
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssUrlRewrite.fallbackCssDataUri"));
  private static final int INDEX_DECLARATION = 1;

  /**
   * {@inheritDoc}
   */
  @Override
  protected Matcher getMatcher(final String content) {
    return PATTERN.matcher(content);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getOriginalDeclaration(final Matcher matcher) {
    return matcher.group(INDEX_DECLARATION);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getOriginalUrl(final Matcher matcher) {
    final String groupA = matcher.group(2);
    final String originalUrl = groupA != null ? groupA : matcher.group(3);
    return originalUrl;
  }
}
