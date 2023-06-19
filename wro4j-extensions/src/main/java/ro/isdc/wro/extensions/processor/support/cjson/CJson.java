/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.support.cjson;

import java.io.InputStream;

import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * The underlying implementation uses <a href="http://stevehanov.ca/blog/index.php?id=104">the cjson project</a>.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public class CJson {
  private static final Logger LOG = LoggerFactory.getLogger(CJson.class);
  private ScriptableObject scope;

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder = null;
      if (scope == null) {
        builder = RhinoScriptBuilder.newClientSideAwareChain().addJSON().evaluateChain(
          getScriptAsStream(), "cjson.js");
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final Exception e) {
      LOG.error("Processing error:" + e.getMessage(), e);
      throw new WroRuntimeException("Processing error", e);
    }
  }


  /**
   * @return stream of the less.js script.
   */
  protected InputStream getScriptAsStream() {
    return CJson.class.getResourceAsStream("cjson.min.js");
  }

  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String pack(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("cjson.pack");
    try {
      final String execute = "CJSON.stringify(JSON.parse(" + WroUtil.toJSMultiLineString(data) + "));";
      final Object result = builder.evaluate(execute, "pack");
      return String.valueOf(result);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }

  public String unpack(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("json.unpack");
    try {
      final String execute = "JSON.stringify(CJSON.parse(" + WroUtil.toJSMultiLineString(data) + "));";
      final Object result = builder.evaluate(execute, "unpack");
      return String.valueOf(result);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
