package ro.isdc.wro.model.resource.processor.impl.css;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.processor.support.CssUrlInspector;
import ro.isdc.wro.model.resource.processor.support.FallbackCssDataUriUrlInspector;


/**
 * <p>Preserves the original css uri along with the new one. This should work also with browsers which do not support
 * dataURI's.</p>
 *
 * <p>Sample Input:</p>
 *
 * <pre>
 * div {
 *   background: url('image.png');
 * }
 * </pre>
 *
 * <p>Sample output:</p>
 *
 * <pre>
 *  div {
 *   background: url('image.png');
 *   background: url('data:image/png;base64,iVBORw0...');
 * }
 * </pre>
 *
 * <p>Applies the graceful degradation technique. For example, if browser can't parse second rule, it'll use first one.</p>
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class FallbackCssDataUriProcessor
    extends CssDataUriPreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(FallbackCssDataUriProcessor.class);
  private static final String SEPARATOR = ";";
  public static final String ALIAS = "fallbackCssDataUri";

  /**
   * {@inheritDoc}
   */
  @Override
  protected String replaceDeclaration(final String originalDeclaration, final String modifiedDeclaration) {
    return originalDeclaration.equals(modifiedDeclaration) ? modifiedDeclaration : computeNewDeclaration(
        originalDeclaration, modifiedDeclaration);
  }

  /**
   * @return the new declaration which contains both: old and new modified declarations.
   */
  private String computeNewDeclaration(final String originalDeclaration, final String modifiedDeclaration) {
    LOG.debug("originalDeclaration: {}", originalDeclaration);
    LOG.debug("modifiedDeclaration: {}", modifiedDeclaration);
    // helps to avoid duplicate unnecessary separator
    final String separator = originalDeclaration.trim().endsWith(SEPARATOR) ? StringUtils.EMPTY : SEPARATOR;
    return originalDeclaration + separator + modifiedDeclaration;
  }

  @Override
  protected CssUrlInspector newCssUrlInspector() {
    return new FallbackCssDataUriUrlInspector();
  }
}
