/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.support.coffeescript;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * CoffeeScript is a little language that compiles into JavaScript. Underneath all of those embarrassing braces and
 * semicolons, JavaScript has always had a gorgeous object model at its heart. CoffeeScript is an attempt to expose the
 * good parts of JavaScript in a simple way.
 * <p/>
 * The underlying implementation use the coffee-script version <code>1.3.3</code> project: {@link https
 * ://github.com/jashkenas/coffee-script}.
 * 
 * @author Alex Objelean
 * @since 1.3.6
 */
public class CoffeeScript {
  private static final Logger LOG = LoggerFactory.getLogger(CoffeeScript.class);
  private String[] options;
  private ScriptableObject scope;
  private static final String DEFAULT_COFFE_SCRIPT = "coffee-script.min.js";
  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder = null;
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().evaluateChain(getCoffeeScriptAsStream(), DEFAULT_COFFE_SCRIPT);
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading init script", ex);
    }
  }


  /**
   * Override this method to use a different version of CoffeeScript. This method is useful for upgrading coffeeScript
   * processor independently of wro4j.
   *
   * @return The stream of the CoffeeScript.
   */
  protected InputStream getCoffeeScriptAsStream() {
    return CoffeeScript.class.getResourceAsStream(DEFAULT_COFFE_SCRIPT);
  }


  /**
   * Validates a js using jsHint and throws {@link LinterException} if the js is invalid. If no exception is thrown, the
   * js is valid.
   *
   * @param data js content to process.
   */
  public String compile(final String data) {
    final StopWatch watch = new StopWatch();
    watch.start("init");
    try {
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start("compile");
      final String compileScript = String.format("CoffeeScript.compile(%s, %s);", WroUtil.toJSMultiLineString(data),
        buildOptions());
      return (String)builder.evaluate(compileScript, "CoffeeScript.compile");
    } finally {
      watch.stop();
      LOG.debug(watch.prettyPrint());
    }
  }


  /**
   * @return A javascript representation of the options. The result is a json object.
   */
  private String buildOptions() {
    final StringBuffer sb = new StringBuffer("{");
    if (options != null) {
      for (int i = 0; i < options.length; i++) {
        sb.append(options[i]).append(": true");
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
    this.options = options == null ? new String[] {} : options;
    LOG.debug("setOptions: {}", Arrays.asList(this.options));
    return this;
  }
}
