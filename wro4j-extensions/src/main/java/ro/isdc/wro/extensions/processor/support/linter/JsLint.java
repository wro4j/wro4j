/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.InputStream;


/**
 * Apply <a href="https://github.com/douglascrockford/JSLint">JsLint</a> script checking utility.
 * <p/>
 * Using untagged version (committed: 2012-06-05 19:38:11)
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
public class JsLint extends AbstractLinter {
  /**
   * The name of the jshint script to be used by default.
   */
  private static final String DEFAULT_JSLINT_JS = "jslint.min.js";

  /**
   * @return the stream of the jshint script. Override this method to provide a different script version.
   */
  @Override
  protected InputStream getScriptAsStream() {
    //this resource is packed with packerJs compressor
    return JsLint.class.getResourceAsStream(DEFAULT_JSLINT_JS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getLinterName() {
    return "JSLINT";
  }
}
