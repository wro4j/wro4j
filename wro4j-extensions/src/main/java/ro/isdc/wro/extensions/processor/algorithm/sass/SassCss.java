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
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * The underlying implementation use the sass.js version <code>0.5.0</code> project:
 * {@link https://github.com/visionmedia/sass.js}.
 *
 * @author Alex Objelean
 */
public class SassCss {
  private static final Logger LOG = LoggerFactory.getLogger(SassCss.class);

  public SassCss() {}


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_NAME = "sass-0.5.0.min.js";
      final InputStream sassStream = getClass().getResourceAsStream(SCRIPT_NAME);
      final String scriptInit = "var exports = {};";
      return RhinoScriptBuilder.newChain().evaluateChain(scriptInit, "initSass").evaluateChain(sassStream, SCRIPT_NAME);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript sass.js", ex);
    }
  }

  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String process(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("sass rendering");
    try {
      final String execute = "exports.render(" + WroUtil.toJSMultiLineString(data) + ");";
      final Object result = builder.evaluate(execute, "sassRender");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
