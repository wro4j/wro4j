/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.jshint;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Apply Packer compressor script using scriptEngine.
 *
 * @author Alex Objelean
 */
public class JsHint {
  private static final Logger LOG = LoggerFactory.getLogger(JsHint.class);
  private String[] options = new String[] {};


  public JsHint() {}


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
   * Validates a js using jsHint and throws {@link JsHintException} if the js is invalid. If no exception is thrown, the
   * js is valid.
   *
   * @param data js content to process.
   */
  public void validate(final String data) throws JsHintException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start("jsHint");
      LOG.debug("options: " + this.options);
      final String packIt = buildJsHintScript(WroUtil.toJSMultiLineString(data), this.options);
      final boolean valid = Boolean.parseBoolean(builder.evaluate(packIt, "check").toString());
      if (!valid) {
        final Object o = builder.evaluate("JSHINT.errors", null);
        LOG.debug("o {}", Context.jsToJava(o, String.class));
        final String json = builder.addJSON().evaluate("JSON.stringify(JSHINT.errors)", "jsHint.errors").toString();
        LOG.debug("json {}", json);
        final Type type = new TypeToken<List<JsError>>() {}.getType();
        final List<JsError> errors = new Gson().fromJson(json, type);
        LOG.debug("errors {}", errors);
        LOG.error("" + json);
        throw new JsHintException().setErrors(errors);
      }
      LOG.debug("result: " + valid);
      watch.stop();
      LOG.debug(watch.prettyPrint());
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Unable to evaluate the script because: " + e.getMessage(), e);
    }
  }


  /**
   * @param data script to pack.
   * @param options options to set as true
   * @return Script used to pack and return the packed result.
   */
  private String buildJsHintScript(final String data, final String... options) {
    final StringBuffer sb = new StringBuffer("{");
    for (int i = 0; i < options.length; i++) {
      sb.append(options[i] + ": true");
      if (i < options.length - 1) {
        sb.append(",");
      }
    }
    sb.append("}");
    LOG.debug("sb {} ", sb);
    return "JSHINT(" + data + ", " + sb.toString() + ");";
  }


  /**
   * @param options the options to set
   */
  public void setOptions(final String ... options) {
    if (options == null) {
      throw new IllegalArgumentException("Options cannot be null!");
    }
    this.options = options;
  }
}
