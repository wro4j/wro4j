/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.algorithm.jsonhpack;

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
 * The underlying implementation use the json.hpack project: {@link https://github.com/WebReflection/json.hpack}.
 *
 * @author Alex Objelean
 * @since 1.3.0
 */
public class JsonHPack {
  private static final Logger LOG = LoggerFactory.getLogger(JsonHPack.class);


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final InputStream scriptStream = getScriptAsStream();
      return RhinoScriptBuilder.newClientSideAwareChain().addJSON().evaluateChain(
          scriptStream, "script.js");
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript script.js", ex);
    } catch (final Exception e) {
      LOG.error("Processing error:" + e.getMessage(), e);
      throw new WroRuntimeException("Processing error", e);
    }
  }


  /**
   * @return stream of the less.js script.
   */
  protected InputStream getScriptAsStream() {
    return getClass().getResourceAsStream("json.hpack.js");
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

    stopWatch.start("json.hpack");
    try {
      final String execute = "JSON.stringify(JSON.hpack(" + WroUtil.toJSMultiLineString(data) + ", 4));";
      final Object result = builder.evaluate(execute, "packIt");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
