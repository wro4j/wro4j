/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.jshint;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Apply Packer compressor script using scriptEngine.
 *
 * @author Alex Objelean
 */
public class JsHint {
  private static final Logger LOG = LoggerFactory.getLogger(JsHint.class);

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      return RhinoScriptBuilder.newChain().evaluateChain(getStreamForJsHint(), "jshint.js");
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading init script", ex);
    }
  }

  private InputStream getStreamForJsHint() {
    return getClass().getResourceAsStream("jshint.js");
  }

  /**
   * @param data js content to process.
   * @return packed js content.
   */
  public boolean isValid(final String data)
    throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start("jsHint");

      final String packIt = buildPackScript(WroUtil.toJSMultiLineString(data));
      final boolean result = Boolean.parseBoolean(builder.evaluateString(packIt, "check").toString());
      if (!result) {
        LOG.error("" + builder.addJSON().evaluate("JSON.stringify(JSHINT.data());", "jsHint.data"));
      }
      LOG.debug("result: " + result);
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return result;
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Unable to evaluate the script because: " + e.getMessage(), e);
    }
  }

  /**
   * @param data script to pack.
   * @return Script used to pack and return the packed result.
   */
  protected String buildPackScript(final String data) {
    return "JSHINT(" + data + ", {});";
  }
}

