package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.util.List;

import ro.isdc.wro.model.resource.Resource;


/**
 * A processor capable of handling <a href="http://lesscss.org/#-importing">LessCss imports</a>
 *
 * @author Alex Objelean
 * @created 4 Mar 2013
 * @since 1.6.3
 */
public class LessCssImportPreProcessor
    extends AbstractCssImportPreProcessor {
  /**
   * {@inheritDoc}
   */
  @Override
  protected String doTransform(final String cssContent, final List<Resource> importedResources)
      throws IOException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<String> findImports(final String css) {
    return super.findImports(css);
  }
}
