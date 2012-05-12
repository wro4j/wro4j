package ro.isdc.wro.extensions.processor.support.dustjs;

import ro.isdc.wro.extensions.processor.support.JsTemplateCompiler;


/**
 * Dust is a JavaScript templating engine designed to provide a clean separation between presentation and logic without
 * sacrificing ease of use. It is particularly well-suited for asynchronous and streaming applications.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 * @created 8 Mar 2012
 */
public class DustJs extends JsTemplateCompiler {
  private static final String DEFAULT_DUST_JS = "dust-full-0.3.0.min.js";

  @Override
  protected String getCompilerPath() {
    return DEFAULT_DUST_JS;
  }

  @Override
  protected String getCompileCommand() {
    return "dust.compile";
  }
}
