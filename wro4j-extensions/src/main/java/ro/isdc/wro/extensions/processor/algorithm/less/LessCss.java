/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.algorithm.less;

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
 * The underlying implementation use the less.js version <code>1.0.41</code> project:
 * {@link https://github.com/cloudhead/less.js}.
 *
 * @author Alex Objelean
 * @since 1.3.0
 */
public class LessCss {
  private static final Logger LOG = LoggerFactory.getLogger(LessCss.class);


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_INIT = "init.js";
      final InputStream initStream = getClass().getResourceAsStream(SCRIPT_INIT);
      final InputStream lessStream = getLessScriptAsStream();
      final String SCRIPT_RUN = "run.js";
      final InputStream runStream = getClass().getResourceAsStream(SCRIPT_RUN);
      return RhinoScriptBuilder.newClientSideAwareChain().evaluateChain(initStream, SCRIPT_INIT).evaluateChain(
          lessStream, "less.js").evaluateChain(runStream, SCRIPT_RUN);
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
  protected InputStream getLessScriptAsStream() {
    return getClass().getResourceAsStream("less-1.1.3.min.js");
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
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
