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
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Apply Packer compressor script using scriptEngine.
 *
 * @author Alex Objelean
 */
public class UglifyJs {
  private static final Logger LOG = LoggerFactory.getLogger(UglifyJs.class);


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_NODE = "node-0.3.0.js";
      final InputStream nodeStream = getClass().getResourceAsStream(SCRIPT_NODE);
      final String SCRIPT_JSON = "json2.js";
      final InputStream jsonStream = getClass().getResourceAsStream(SCRIPT_JSON);
      final String SCRIPT_PARSE = "parse-js.js";
      final InputStream parseStream = getClass().getResourceAsStream(SCRIPT_PARSE);
      final String SCRIPT_PROCESS = "process.js";
      final InputStream processStream = getClass().getResourceAsStream(SCRIPT_PROCESS);

      final String scriptInit = "var exports = {}; function require() {return exports;}; var process={version:0.3};";

      return RhinoScriptBuilder.newChain().evaluateChain(scriptInit, "initScript").evaluateChain(nodeStream,
        SCRIPT_NODE).evaluateChain(jsonStream, SCRIPT_JSON).evaluateChain(parseStream, SCRIPT_PARSE).evaluateChain(
       processStream, SCRIPT_PROCESS);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
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
      watch.start("pack");

      final String originalCode = WroUtil.toJSMultiLineString(code);
      final StringBuffer sb = new StringBuffer("(function() {");
      sb.append("var orig_code = " + originalCode + ";");
      sb.append("var ast = exports.parse(orig_code);");
      sb.append("ast = exports.ast_mangle(ast);");
      sb.append("ast = exports.ast_squeeze(ast);");
      sb.append("return exports.gen_code(ast);");
      sb.append("})();");

      final Object result = builder.evaluate(sb.toString(), "uglifyIt");
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Unable to evaluate the script because: " + e.getMessage(), e);
    }
  }


  public static void main(final String[] args)
    throws Exception {
    System.out.println(new UglifyJs().process("[a[0]]"));
  }
}
