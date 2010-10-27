/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.algorithm.sass;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;


/**
 * Sass implementation.
 *
 * @author Alex Objelean
 */
public class SassCSS {
  private static final Logger LOG = LoggerFactory.getLogger(SassCSS.class);

  public SassCSS() {}


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_LESS = "sass-0.5.0.js";
      final InputStream lessStream = getClass().getResourceAsStream(SCRIPT_LESS);
      final String scriptInitSass = "var exports = {};";
      return RhinoScriptBuilder.newChain().evaluateChain(scriptInitSass, "initSass").evaluateChain(lessStream, SCRIPT_LESS);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript sass.js", ex);
    }
  }

  /**
   * Replace new line characters with empty spaces.
   * @param data
   * @return
   */
  private static String removeNewLines(final String data) {
    return data.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
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

    stopWatch.start("sass rendering");
    try {
      final String lessitjs = "exports.render(\"" + removeNewLines(data) + "\");";
      final Object result = builder.evaluate(lessitjs, "sassRender");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Could not execute the script", e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
