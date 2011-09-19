/*
 *  Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
 * Encapsulates common code for utilities like jsHint or jsLint.
 *
 * @author Alex Objelean
 * @created 19 Sept 2011
 * @since 1.4.2
 */
public abstract class AbstractLinter {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractLinter.class);
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
        builder = RhinoScriptBuilder.newChain().evaluateChain(getScriptAsStream(), "linter.js");
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
  protected abstract InputStream getScriptAsStream();


  /**
   * Validates a js using jsHint and throws {@link LinterException} if the js is invalid. If no exception is thrown, the
   * js is valid.
   *
   * @param data js content to process.
   */
  public void validate(final String data) throws LinterException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start("lint");
      LOG.debug("options: {}", Arrays.toString(this.options));
      final String packIt = buildJsHintScript(WroUtil.toJSMultiLineString(data), this.options);
      final boolean valid = Boolean.parseBoolean(builder.evaluate(packIt, "check").toString());
      if (!valid) {
        final String json = builder.addJSON().evaluate(String.format("JSON.stringify(%s.errors)", getLinterName()),
            "stringify errors").toString();
        LOG.debug("json {}", json);
        final Type type = new TypeToken<List<LinterError>>() {}.getType();
        final List<LinterError> errors = new Gson().fromJson(json, type);
        LOG.debug("errors {}", errors);
        throw new LinterException().setErrors(errors);
      }
      LOG.debug("result: {}", valid);
      watch.stop();
      LOG.debug(watch.prettyPrint());
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    }
  }

  /**
   * @return the name of the function used to perform the lint operation.
   */
  protected abstract String getLinterName();

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
    final String script = String.format("%s(%s,%s);", getLinterName(), data, buildOptions(options));
    return script;
  }


  /**
   * @param options
   *          an array of options as provided by user.
   * @return the json object containing options to be used by JsHint.
   */
  protected String buildOptions(final String... options) {
    final StringBuffer sb = new StringBuffer("{");
    if (options != null) {
      for (int i = 0; i < options.length; i++) {
        final String option = options[i];
        if (!StringUtils.isEmpty(option)) {
          sb.append(processSingleOption(option));
          if (i < options.length - 1) {
            sb.append(",");
          }
        }
      }
    }
    sb.append("}");
    LOG.debug("options: {}", options);
    return sb.toString();
  }


  /**
   * @return
   */
  private String processSingleOption(final String option) {
    String optionName = option;
    String optionValue = Boolean.TRUE.toString();
    if (option.contains("=")) {
      final String[] optionEntry = option.split("=");
      if (optionEntry.length != 2) {
        throw new IllegalArgumentException("Invalid option provided: " + option);
      }
      optionName = optionEntry[0];
      optionValue = option.split("=")[1];
    }
    return String.format("\"%s\": %s", optionName.trim(), optionValue.trim());
  }

  /**
   * @param options the options to set
   */
  public AbstractLinter setOptions(final String ... options) {
    LOG.debug("setOptions: {}", options);
    this.options = options == null ? new String[] {} : options;
    return this;
  }
}
