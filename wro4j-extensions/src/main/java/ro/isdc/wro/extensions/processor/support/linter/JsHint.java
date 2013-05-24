/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.IOException;
import java.io.InputStream;


/**
 * Apply JsHint script checking utility.
 * <p/>
 * Using untagged version (committed: 2012-11-13 05:25:37). <br/>
 * The jshint script was slightly modified, by removing the part where the arguments are processed, since it is not
 * used.
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHint extends AbstractLinter {
  /**
   * The name of the jshint script to be used by default.
   */
  private static final String DEFAULT_JSHINT_JS = "jshint.min.js";

  /**
   * @return the stream of the jshint script. Override this method to provide a different script version.
   */
  @Override
  protected InputStream getScriptAsStream() throws IOException {
    //this resource is packed with packerJs compressor
    return JsHint.class.getResourceAsStream(DEFAULT_JSHINT_JS);
    //return getWebjarLocator().locate(WebjarUriLocator.createUri("jshint.js"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getLinterName() {
    return "JSHINT";
  }
}
