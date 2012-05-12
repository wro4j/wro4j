package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.FilenameUtils;
import ro.isdc.wro.extensions.processor.support.JsTemplateCompiler;
import ro.isdc.wro.extensions.processor.support.dustjs.DustJs;
import ro.isdc.wro.model.resource.Resource;

/**
 * A processor for dustJs template framework. Uses <a href="http://akdubya.github.com/dustjs/">dustjs</a> library to
 * transform a template into plain javascript.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 * @created 8 Mar 2012
 */
public class DustJsProcessor extends JsTemplateCompilerProcessor {
  public static final String ALIAS = "dustJs";

  @Override
  protected JsTemplateCompiler createCompiler() {
    return new DustJs();
  }

  @Override
  protected String getArgument(Resource resource) {
    final String name = resource == null ? "" : FilenameUtils.getBaseName(resource.getUri());
    return String.format("'%s'", name);
  }
}
