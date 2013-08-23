/*
 *  Copyright wro4j@2011.
 */
package ro.isdc.wro.extensions.processor.support.less;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.locator.WebjarUriLocator;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * This class is not thread-safe.<br/>
 * The underlying implementation uses the webjar containing less.js library.
 *
 * @author Alex Objelean
 * @since 1.3.0
 */
public class LessCss {
  private static final Logger LOG = LoggerFactory.getLogger(LessCss.class);
  /**
   * The name of the sass script to be used by default.
   */
  public static final String DEFAULT_LESS_JS = "less-1.3.3.min.js";
  private static final String SCRIPT_INIT = "init.js";
  private WebjarUriLocator webjarLocator;
  private ScriptableObject scope;

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder = null;
      if (scope == null) {
        final InputStream initStream = LessCss.class.getResourceAsStream(SCRIPT_INIT);
        builder = RhinoScriptBuilder.newClientSideAwareChain().evaluateChain(initStream, SCRIPT_INIT).evaluateChain(
            getScriptAsStream(), DEFAULT_LESS_JS);
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
    } catch (final Exception e) {
      LOG.error("Processing error:" + e.getMessage(), e);
      throw new WroRuntimeException("Processing error", e);
    }
  }

  /**
   * @return stream of the less.js script.
   */
  protected InputStream getScriptAsStream() throws IOException {
    return getWebjarLocator().locate(WebjarUriLocator.createUri("less.min.js"));
  }

  /**
   * @return {@link WebjarUriLocator} instance to retrieve webjars.
   */
  private WebjarUriLocator getWebjarLocator() {
    if (webjarLocator == null) {
      webjarLocator = new WebjarUriLocator();
    }
    return webjarLocator;
  }

  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String less(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("lessify");
    try {
      final String execute = "lessIt(" + WroUtil.toJSMultiLineString(data) + ");";
      final Object result = builder.evaluate(execute, "lessIt");
      return String.valueOf(result);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
