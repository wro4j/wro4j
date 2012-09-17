/*
 * Copyright wro4j@2011.
 */
package ro.isdc.wro.extensions.processor.support.csslint;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.support.linter.OptionsBuilder;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * CssLint script engine utility. The underlying implementation uses CSSLint script utility<br/>
 * {@link https ://github.com/stubbornella/csslint}. The underlying csslint version is 0.9.9.
 * 
 * @author Alex Objelean
 * @since 1.3.8
 * @created 19 Jun 2011
 */
public class CssLint {
  private static final Logger LOG = LoggerFactory.getLogger(CssLint.class);
  /**
   * The name of the csslint script to be used by default.
   */
  private static final String DEFAULT_CSSLINT_JS = "csslint.min.js";
  private final OptionsBuilder optionsBuilder = new OptionsBuilder();
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
        builder = RhinoScriptBuilder.newChain().evaluateChain(getScriptAsStream(), DEFAULT_CSSLINT_JS);
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
   * @return the stream of the csslint script. Override this method to provide a different script version.
   */
  protected InputStream getScriptAsStream() {
    return CssLint.class.getResourceAsStream(DEFAULT_CSSLINT_JS);
  }
  
  /**
   * Validates a js using jsHint and throws {@link CssLintException} if the js is invalid. If no exception is thrown,
   * the js is valid.
   * 
   * @param data
   *          js content to process.
   * @throws CssLintException
   *           when parsed css has some kind of problems.
   */
  public void validate(final String data)
      throws CssLintException {
    final StopWatch watch = new StopWatch();
    watch.start("init");
    final RhinoScriptBuilder builder = initScriptBuilder();
    watch.stop();
    watch.start("cssLint");
    LOG.debug("options: {}", Arrays.toString(this.options));
    final String script = buildCssLintScript(WroUtil.toJSMultiLineString(data), this.options);
    LOG.debug("script: {}", script);
    builder.evaluate(script, "CSSLint.verify").toString();
    final boolean valid = Boolean.parseBoolean(builder.evaluate("result.length == 0", "checkNoErrors").toString());
    if (!valid) {
      final String json = builder.addJSON().evaluate("JSON.stringify(result)", "CssLint messages").toString();
      LOG.debug("json {}", json);
      final Type type = new TypeToken<List<CssLintError>>() {}.getType();
      final List<CssLintError> errors = new Gson().fromJson(json, type);
      LOG.debug("Errors: {}", errors);
      throw new CssLintException().setErrors(errors);
    }
    LOG.debug("isValid: {}", valid);
    watch.stop();
    LOG.debug(watch.prettyPrint());
  }
  
  private String buildCssLintScript(final String data, final String... options) {
    return String.format("var result = CSSLint.verify(%s,%s).messages", data, optionsBuilder.build(options));
  }
  
  /**
   * @param options
   *          the options to set
   */
  public CssLint setOptions(final String... options) {
    LOG.debug("setOptions: {}", options);
    if (options != null) {
      this.options = options.length > 1 ? options : optionsBuilder.splitOptions(options[0]);
    } else {
      this.options = ArrayUtils.EMPTY_STRING_ARRAY;
    }
    
    return this;
  }
}
