/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.coffeescript;

import java.io.IOException;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHintException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * CoffeeScript is a little language that compiles into JavaScript. Underneath all of those embarrassing braces and
 * semicolons, JavaScript has always had a gorgeous object model at its heart. CoffeeScript is an attempt to expose the
 * good parts of JavaScript in a simple way.
 *
 * <p/>
 *
 * @author Alex Objelean
 * @since 1.3.6
 */
public class CoffeeScript {
  private static final Logger LOG = LoggerFactory.getLogger(CoffeeScript.class);
  private String[] options;
  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      return RhinoScriptBuilder.newChain().evaluateChain(getClass().getResourceAsStream("coffee-script-1.0.1.js"),
        "coffee-script-1.0.1.js");
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading init script", ex);
    }
  }



  /**
   * Validates a js using jsHint and throws {@link JsHintException} if the js is invalid. If no exception is thrown, the
   * js is valid.
   *
   * @param data js content to process.
   */
  public String compile(final String data) {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start("compile");
      final String compileScript = String.format("CoffeeScript.compile(%s, %s);", WroUtil.toJSMultiLineString(data),
        buildOptions());
      final String result = (String)builder.evaluate(compileScript, "CoffeeScript.compile");
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return result;
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    }
  }


  /**
   * @return A javascript representation of the options. The result is a json object.
   */
  private String buildOptions() {
    final StringBuffer sb = new StringBuffer("{");
    if (options != null) {
      for (int i = 0; i < options.length; i++) {
        sb.append(options[i] + ": true");
        if (i < options.length - 1) {
          sb.append(",");
        }
      }
    }
    sb.append("}");
    return sb.toString();
  }


  /**
   * @param options the options to set
   */
  public CoffeeScript setOptions(final String... options) {
    LOG.debug("setOptions: {}", options);
    this.options = options == null ? new String[] {} : options;
    return this;
  }
}
