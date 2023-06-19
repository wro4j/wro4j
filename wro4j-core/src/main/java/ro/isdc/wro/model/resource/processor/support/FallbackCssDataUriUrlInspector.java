package ro.isdc.wro.model.resource.processor.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ro.isdc.wro.util.WroUtil;


/**
 * Similar to {@link CssUrlInspector} but responsible for Preserving the original css uri along with the new one. This
 * should work also with browsers which do not support dataURI's.
 *
 * @author Alex Objelean
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
    return groupA != null ? groupA : matcher.group(3);
  }
}
