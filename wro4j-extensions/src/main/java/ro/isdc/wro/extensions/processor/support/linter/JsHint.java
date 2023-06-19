/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;


/**
 * <p>Apply JsHint script checking utility.</p>
 *
 * <p>Using untagged version (committed: 2012-11-13 05:25:37). The jshint script was slightly modified, by removing the part where the arguments are processed, since it is not
 * used.</p>
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHint extends AbstractLinter {
  /**
   * @return the stream of the jshint script. Override this method to provide a different script version.
   */
  @Override
  protected InputStream getScriptAsStream() throws IOException {
    return getWebjarLocator().locate(WebjarUriLocator.createUri("dist/jshint.js"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getLinterName() {
    return "JSHINT";
  }
}
