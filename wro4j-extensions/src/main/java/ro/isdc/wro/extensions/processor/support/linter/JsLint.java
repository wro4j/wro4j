/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;


/**
 * Apply <a href="https://github.com/douglascrockford/JSLint">JsLint</a> script checking utility.
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
public class JsLint extends AbstractLinter {
  /**
   * @return the stream of the jslint script. Override this method to provide a different script version.
   */
  @Override
  protected InputStream getScriptAsStream() throws IOException {
    return getWebjarLocator().locate(WebjarUriLocator.createUri("jslint.js"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getLinterName() {
    return "JSLINT";
  }
}
