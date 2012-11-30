package ro.isdc.wro.model.resource.processor.impl.css;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Preserves the original css uri along with the new one. This should work also with browsers which do not support
 * dataURI's.
 * <p/>
 * Sample Input:
 * 
 * <pre>
 * div {
 *   background: url('image.png');
 * }
 * </pre>
 * 
 * Sample output:
 * 
 * <pre>
 *  div {
 *   background: url('image.png');
 *   background: url('data:image/png;base64,iVBORw0...');
 * }
 * </pre>
 * 
 * Applies the graceful degradation technique. For example, if browser can't parse second rule, it'll use first one.
 * 
 * @author Alex Objelean
 * @created 4 Jun 2012
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
    String separator = originalDeclaration.trim().endsWith(SEPARATOR) ? StringUtils.EMPTY : SEPARATOR;
    return originalDeclaration + separator + modifiedDeclaration;
  }

    @Override
    protected String getRegexPatternKey() {
        return "cssUrlRewrite.fallbackCssDataUriProcessor";
    }

    @Override
    public int getDeclarationIndex() {
        return 1;
    }

    @Override
    protected int getUrlIndexA() {
        return 2;
    }

    @Override
    protected int getUrlIndexB() {
        return 3;
    }
}
