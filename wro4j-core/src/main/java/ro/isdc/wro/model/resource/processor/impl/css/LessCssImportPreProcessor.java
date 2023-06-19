package ro.isdc.wro.model.resource.processor.impl.css;

import java.util.List;

import ro.isdc.wro.model.resource.processor.support.LessCssImportInspector;


/**
 * A processor capable of handling <a href="http://lesscss.org/#-importing">LessCss imports</a>
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public class LessCssImportPreProcessor
    extends CssImportPreProcessor {

  public static final String ALIAS = "lessCssImport";
  /**
   * {@inheritDoc}
   */
  @Override
  protected List<String> findImports(final String css) {
    return new LessCssImportInspector(css).findImports();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String removeImportStatements(final String cssContent) {
    return new LessCssImportInspector(cssContent).removeImportStatements();
  }
}
