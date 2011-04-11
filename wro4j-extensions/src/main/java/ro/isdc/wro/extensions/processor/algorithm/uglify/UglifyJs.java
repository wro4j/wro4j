/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.uglify;

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
 * The underlying implementation use the less.js version <code>1.0.1</code> project: {@link https://github.com/mishoo/UglifyJS}.
 *
 * @author Alex Objelean
 */
public class UglifyJs {
  private static final Logger LOG = LoggerFactory.getLogger(UglifyJs.class);
  private final boolean uglify;


  /**
   * @param uglify if true the code will be uglified (compressed and minimized), otherwise it will be beautified (nice
   *        formatted).
   */
  private UglifyJs(final boolean uglify) {
    this.uglify = uglify;
  }


  /**
   * Factory method for creating the uglifyJs engine.
   */
  public static UglifyJs uglifyJs() {
    return new UglifyJs(true);
  }


  /**
   * Factory method for creating the beautifyJs engine.
   */
  public static UglifyJs beautifyJs() {
    return new UglifyJs(false);
  }


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_PARSE = "parse-js-1.0.1.min.js";
      final InputStream parseStream = getClass().getResourceAsStream(SCRIPT_PARSE);
      final String SCRIPT_PROCESS = "process-1.0.1.min.js";
      final InputStream processStream = getClass().getResourceAsStream(SCRIPT_PROCESS);

      final String scriptInit = "var exports = {}; function require() {return exports;}; var process={version:0.1};";
      return RhinoScriptBuilder.newChain().addJSON().evaluateChain(scriptInit, "initScript").evaluateChain(parseStream,
        SCRIPT_PARSE).evaluateChain(processStream, SCRIPT_PROCESS);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed initializing js", ex);
    }
  }


  /**
   * @param data js content to process.
   * @return packed js content.
   */
  public String process(final String code)
    throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start(uglify ? "uglify" : "beautify");

      final String originalCode = WroUtil.toJSMultiLineString(code);
      final StringBuffer sb = new StringBuffer("(function() {");
      sb.append("var orig_code = " + originalCode + ";");
      sb.append("var ast = jsp.parse(orig_code);");
      sb.append("ast = exports.ast_mangle(ast);");
      sb.append("ast = exports.ast_squeeze(ast);");
      // the second argument is true for uglify and false for beautify.
      sb.append("return exports.gen_code(ast, {beautify: " + !uglify + " });");
      sb.append("})();");

      final Object result = builder.evaluate(sb.toString(), "uglifyIt");
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    }
  }
}
