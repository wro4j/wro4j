/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.jshint;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.csslint.CssLint;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Apply JsHint script checking utility.
 * <p/>
 * Using untagged version (commited: 2011-09-05 11:13:44)
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHint {
  private static final Logger LOG = LoggerFactory.getLogger(JsHint.class);
  /**
   * The name of the jshint script to be used by default.
   */
  private static final String DEFAULT_JSHINT_JS = "jshint.min.js";
  /**
   * Options to apply to js hint processing
   */
  private String[] options;
  private ScriptableObject scope;

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder = null;
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().evaluateChain(getScriptAsStream(), DEFAULT_JSHINT_JS);
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException e) {
      throw new WroRuntimeException("Failed reading init script", e);
    }
  }

  /**
   * @return the stream of the jshint script. Override this method to provide a different script version.
   */
  protected InputStream getScriptAsStream() {
    //this resource is packed with packerJs compressor
    return getClass().getResourceAsStream(DEFAULT_JSHINT_JS);
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
      LOG.debug("options: {}", Arrays.toString(this.options));
      final String packIt = buildJsHintScript(WroUtil.toJSMultiLineString(data), this.options);
      final boolean valid = Boolean.parseBoolean(builder.evaluate(packIt, "check").toString());
      if (!valid) {
        final String json = builder.addJSON().evaluate("JSON.stringify(JSHINT.errors)", "jsHint.errors").toString();
        LOG.debug("json {}", json);
        final Type type = new TypeToken<List<JsHintError>>() {}.getType();
        final List<JsHintError> errors = new Gson().fromJson(json, type);
        LOG.debug("errors {}", errors);
        throw new JsHintException().setErrors(errors);
      }
      LOG.debug("result: {}", valid);
      watch.stop();
      LOG.debug(watch.prettyPrint());
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    }
  }

  /**
   * TODO this method is duplicated in {@link CssLint}. Extract and reuse it.
   *
   * @param data
   *          script to process.
   * @param options
   *          options to set as true
   * @return Script used to pack and return the packed result.
   */
  private String buildJsHintScript(final String data, final String... options) {
    final StringBuffer sb = new StringBuffer("{");
    if (options != null) {
      for (int i = 0; i < options.length; i++) {
        sb.append("\"" + options[i] + "\": true");
        if (i < options.length - 1) {
          sb.append(",");
        }
      }
    }
    sb.append("}");
    LOG.debug("jsHint Options: {}", sb);
    //return "JSHINT(" + data + ", " + sb.toString() + ");";
    return "JSHINT(" + data + ", " + sb + ");";
  }


  /**
   * @param options the options to set
   */
  public JsHint setOptions(final String ... options) {
    LOG.debug("setOptions: {}", options);
    this.options = options == null ? new String[] {} : options;
    return this;
  }
}
