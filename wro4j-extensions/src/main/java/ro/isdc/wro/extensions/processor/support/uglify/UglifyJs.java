/*
 * Copyright wro4j@2011.
 */
package ro.isdc.wro.extensions.processor.support.uglify;

import static ro.isdc.wro.extensions.processor.support.uglify.UglifyJs.Type.UGLIFY;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * The underlying implementation use the uglifyJs untagged version: 1.2.7 (last commit date: 2012-05-08 21:35:42).
 * <p/>
 * {@link https://github.com/mishoo/UglifyJS}.
 * <p/>
 * The uglify script is resulted from merging of the following two scripts: parse-js.js, process.js.
 * 
 * @author Alex Objelean
 * @since 1.3.1
 */
public class UglifyJs {
  private static final Logger LOG = LoggerFactory.getLogger(UglifyJs.class);
  /**
   * The name of the uglify script to be used by default.
   */
  public static final String DEFAULT_UGLIFY_JS = "uglifyJs.min.js";
  private String invokeScript;
  private String defaultOptionsAsJson;
  /**
   * If true, the script is uglified, otherwise it is beautified.
   */
  private final boolean uglify;
  /**
   * Comma delimited variable names to have uglify not mangle
   */
  private String reservedNames;
  private ScriptableObject scope;
  
  /**
   * The type of processing supported by UglifyJs library. This enum replaces ugly boolean constructor parameter.
   */
  public static enum Type {
    BEAUTIFY, UGLIFY
  }
  
  /**
   * @return the script responsible for invoking the uglifyJs script.
   */
  private String getInvokeScript()
      throws IOException {
    if (invokeScript == null) {
      invokeScript = IOUtils.toString(UglifyJs.class.getResourceAsStream("invoke.js"));
    }
    return invokeScript;
  }
  
  /**
   * @param uglify
   *          if true the code will be uglified (compressed and minimized), otherwise it will be beautified (nice
   *          formatted).
   */
  public UglifyJs(final Type uglifyType) {
    Validate.notNull(uglifyType);
    this.uglify = uglifyType == UGLIFY ? true : false;
  }
  
  /**
   * Factory method for creating the uglifyJs engine.
   */
  public static UglifyJs uglifyJs() {
    return new UglifyJs(UGLIFY);
  }
  
  /**
   * Factory method for creating the beautifyJs engine.
   */
  public static UglifyJs beautifyJs() {
    return new UglifyJs(Type.BEAUTIFY);
  }
  
  /**
   * some libraries rely on certain names to be used, so this option allow you to exclude such names from the mangler.
   * For example, to keep names require and $super intact you'd specify –reserved-names "require,$super".
   * 
   * @param reservedNames
   *          the reservedNames to set
   */
  public UglifyJs setReservedNames(final String reservedNames) {
    this.reservedNames = reservedNames;
    return this;
  }
  
  /**
   * @return not null value representing reservedNames.
   */
  private String getReservedNames() {
    return this.reservedNames == null ? "" : reservedNames;
  }
  
  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    // TODO: Find a way to encapsulate this code
    RhinoScriptBuilder builder = null;
    try {
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().addJSON().evaluateChain(UglifyJs.class.getResourceAsStream("init.js"),
            "initScript").evaluateChain(getScriptAsStream(), DEFAULT_UGLIFY_JS);
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final Exception ex) {
      throw new IllegalStateException("Failed initializing js", ex);
    }
  }
  
  /**
   * @return the stream of the uglify script. Override this method to provide a different script version.
   */
  protected InputStream getScriptAsStream() {
    return UglifyJs.class.getResourceAsStream(DEFAULT_UGLIFY_JS);
  }
  
  /**
   * @param data
   *          js content to process.
   * @return packed js content.
   */
  public String process(final String filename, final String code)
      throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init " + filename);
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      final String originalCode = WroUtil.toJSMultiLineString(code);
      // TODO handle reservedNames
      final String optionsAsJson = createOptionsAsJson();
      Validate.notNull(optionsAsJson);
      final String invokeScript = String.format(getInvokeScript(), originalCode, optionsAsJson);
      watch.start(uglify ? "uglify" : "beautify");
      final Object result = builder.evaluate(invokeScript.toString(), "uglifyIt");
      
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    } 
  }
  
  /**
   * Reads by default options from options.js file located in the same package. This is an example of how the options
   * could look like:
   * 
   * <pre>
   * {
   *    codegen_options: {
   *      beautify: false,
   *      space_colon: false
   *    },
   *    squeeze: true,
   *    dead_code: true,
   *    mangle: true
   * }
   * </pre>
   * 
   * @return json representation of options.
   */
  protected String createOptionsAsJson()
      throws IOException {
    return String.format(getDefaultOptions(), !uglify, getReservedNames());
  }
  
  /**
   * @return default options string representation loaded from options.js resource file.
   */
  private String getDefaultOptions()
      throws IOException {
    if (defaultOptionsAsJson == null) {
      defaultOptionsAsJson = IOUtils.toString(UglifyJs.class.getResourceAsStream("options.js"));
    }
    return defaultOptionsAsJson;
  }
}
