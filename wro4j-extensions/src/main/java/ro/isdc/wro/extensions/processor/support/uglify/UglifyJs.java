/*
 *  Copyright wro4j@2011.
 */
package ro.isdc.wro.extensions.processor.support.uglify;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
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
 * The underlying implementation use the uglifyJs version (1.1.2-SNAPSHOT) <br/>
 * committed at the following date: 2011-12-03 17:10:36.
 * <p/>
 * {@link https://github.com/mishoo/UglifyJS}.
 * <p/>
 * The uglify script is resulted from merging of the following two scripts: parse-js.js, process.js. The final version
 * is compressed with googleClosure compressor (simple mode), because it is quite efficient and doesn't break the code.
 *
 * @author Alex Objelean
 * @since 1.3.1
 */
public class UglifyJs {
  private static final Logger LOG = LoggerFactory.getLogger(UglifyJs.class);
  /**
   * The name of the uglify script to be used by default.
   */
  private static final String DEFAULT_UGLIFY_JS = "uglifyJs.min.js";
  /**
   * If true, the script is uglified, otherwise it is beautified.
   */
  private final boolean uglify;
  /**
   * Comma delimited variable names to have uglify not mangle
   */
  private final String reservedNames;
  private ScriptableObject scope;


  /**
   * @param uglify if true the code will be uglified (compressed and minimized), otherwise it will be beautified (nice
   *        formatted).
   */
  public UglifyJs(final boolean uglify, final String reservedNames) {
    this.uglify = uglify;
    this.reservedNames = reservedNames;
  }


  /**
   * Factory method for creating the uglifyJs engine.
   */
  public static UglifyJs uglifyJs() {
    return new UglifyJs(true, "");
  }


  /**
   * Factory method for creating the uglifyJs engine.
   */
  public static UglifyJs uglifyJs(String reservedNames) {
    return new UglifyJs(true, reservedNames);
  }


  /**
   * Factory method for creating the beautifyJs engine.
   */
  public static UglifyJs beautifyJs() {
    return new UglifyJs(false, "");
  }


  /**
   * Factory method for creating the beautifyJs engine.
   */
  public static UglifyJs beautifyJs(String reservedNames) {
    return new UglifyJs(false, reservedNames);
  }

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    //TODO: Find a way to encapsulate this code
    RhinoScriptBuilder builder = null;
    try {
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().addJSON().evaluateChain(getClass().getResourceAsStream("init.js"),
          "initScript").evaluateChain(getScriptAsStream(), DEFAULT_UGLIFY_JS);
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed initializing js", ex);
    }
  }


  /**
   * @return the stream of the uglify script. Override this method to provide a different script version.
   */
  protected InputStream getScriptAsStream() {
    return getClass().getResourceAsStream(DEFAULT_UGLIFY_JS);
  }


  /**
   * @param data js content to process.
   * @return packed js content.
   */
  public String process(String filename, final String code)
    throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init " + filename);
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      final String originalCode = WroUtil.toJSMultiLineString(code);
      final String invokeScript = String.format(IOUtils.toString(getClass().getResourceAsStream("invoke.js")),
        originalCode, reservedNames, !uglify);
      watch.start(uglify ? "uglify" : "beautify");
      final Object result = builder.evaluate(invokeScript.toString(), "uglifyIt");

      watch.stop();
      LOG.debug(watch.prettyPrint());
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    }
  }
}
