/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.support.sass;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * The underlying implementation use <a href="https://github.com/visionmedia/sass.js">the sass.js project</a> version <code>0.5.0</code>.
 *
 * @author Alex Objelean
 */
public class SassCss {
  private static final Logger LOG = LoggerFactory.getLogger(SassCss.class);
  /**
   * The name of the sass script to be used by default.
   */
  public static final String DEFAULT_SASS_JS = "sass-0.5.0.min.js";
  private ScriptableObject scope;

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder = null;
      if (scope == null) {
        final String scriptInit = "var exports = {};";
        builder = RhinoScriptBuilder.newChain().evaluateChain(scriptInit, "initSass").evaluateChain(
          getScriptAsStream(), DEFAULT_SASS_JS);
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException ex) {
      throw new WroRuntimeException("Failed reading javascript sass.js", ex);
    }
  }

  /**
   * @return the stream of the uglify script. Override this method to provide a different script version.
   */
  protected InputStream getScriptAsStream() {
    return SassCss.class.getResourceAsStream(DEFAULT_SASS_JS);
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
      // replace tabs with spaces, since the script doesn't handle well tabs (throws exception).
      // dataWithoutTabs = data;
      final String execute = "exports.render(" + WroUtil.toJSMultiLineString(data) + ");";
      final Object result = builder.evaluate(execute, "sassRender");
      return String.valueOf(result);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
