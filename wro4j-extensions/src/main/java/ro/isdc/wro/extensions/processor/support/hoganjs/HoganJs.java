package ro.isdc.wro.extensions.processor.support.hoganjs;

import ro.isdc.wro.extensions.processor.support.JsTemplateCompiler;


/**
 * Hogan.js: http://twitter.github.com/hogan.js/
 *
 * @author Eivind Barstad Waaler
 * @created 10 May 2012
 */
public class HoganJs extends JsTemplateCompiler {
  private static final String DEFAULT_HOGAN_JS = "hogan-2.0.0.min.js";

  @Override
  protected String getCompilerPath() {
    return DEFAULT_HOGAN_JS;
  }

  @Override
  protected String getCompileCommand() {
    return "Hogan.compile";
  }

  @Override
  protected String getArguments() {
    return "{asString: true}";
  }
}
