package ro.isdc.wro.model.resource.processor.impl.css;

/**
 * Preserves the original css uri along with the new one. This should work also with browsers which do not support
 * dataURI's.
 * <p/>
 * Sample Input:
 * <pre>
 * div {
 *   background: url('image.png');
 * }
 * </pre>
 * Sample output:
 * <pre>
 *  div {
 *   background: url('image.png');
 *   background: url('data:image/png;base64,iVBORw0...');
 * }
 * </pre>
 * Applies the graceful degradation technique. For example, if browser can't parse second rule, it'll use first one.
 * 
 * @author Alex Objelean
 * @created 4 Jun 2012
 * @since 1.4.7
 */
public class FallbackCssDataUriProcessor
    extends CssDataUriPreProcessor {
  public static final String ALIAS = "fallbackCssDataUri";
  /**
   * {@inheritDoc}
   */
  @Override
  protected String replaceDeclaration(final String originalExpression, final String modifiedExpression) {
    System.out.println("originalExpression: " + originalExpression);
    System.out.println("modifiedExpression: " + modifiedExpression);
    return originalExpression.equals(modifiedExpression) ? modifiedExpression : originalExpression + ";"
        + modifiedExpression;
  }
}
