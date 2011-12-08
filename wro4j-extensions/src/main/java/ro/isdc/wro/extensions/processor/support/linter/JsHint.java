/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.InputStream;


/**
 * Apply JsHint script checking utility.
 * <p/>
 * Using untagged version (commited: 2011-11-29 09:47:27)
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHint extends AbstractLinter {
  /**
   * The name of the jshint script to be used by default.
   */
  private static final String DEFAULT_JSLINT_JS = "jshint.min.js";

  /**
   * @return the stream of the jshint script. Override this method to provide a different script version.
   */
  @Override
  protected InputStream getScriptAsStream() {
    //this resource is packed with packerJs compressor
    return getClass().getResourceAsStream(DEFAULT_JSLINT_JS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getLinterName() {
    return "JSHINT";
  }
}
