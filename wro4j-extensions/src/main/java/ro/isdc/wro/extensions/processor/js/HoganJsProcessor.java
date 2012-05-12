package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.JsTemplateCompiler;
import ro.isdc.wro.extensions.processor.support.hoganjs.HoganJs;

/**
 * A processor for hogan.js template framework. Uses <a href="http://twitter.github.com/hogan.js/">hogan.js</a> library to
 * transform a template into plain javascript.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 * @created 8 Mar 2012
 */
public class HoganJsProcessor extends JsTemplateCompilerProcessor {
  public static final String ALIAS = "hoganJs";

  @Override
  protected JsTemplateCompiler createCompiler() {
    return new HoganJs();
  }
}
