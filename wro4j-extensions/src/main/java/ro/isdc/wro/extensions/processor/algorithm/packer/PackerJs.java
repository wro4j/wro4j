/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.packer;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;


/**
 * Apply Packer compressor script using scriptEngine.
 *
 * @author Alex Objelean
 */
public class PackerJs {
  private static final Logger LOG = LoggerFactory.getLogger(PackerJs.class);

  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_BASE2 = "base2.js";
      final InputStream lessStream = getClass().getResourceAsStream(SCRIPT_BASE2);
      final String SCRIPT_PACKER = "packer.js";
      final InputStream runStream = getClass().getResourceAsStream(SCRIPT_PACKER);

      return RhinoScriptBuilder.newChain().evaluateChain(lessStream, SCRIPT_BASE2).evaluateChain(runStream,
        SCRIPT_PACKER);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
    }
  }

  /**
   * @param data js content to process.
   * @return packed js content.
   */
  public String pack(final String data)
    throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start("pack");
      final String script = multilineEscape(data);

      final String packIt = "new Packer().pack(\"" + script + "\", true, true);";
      final Object result = builder.evaluate(packIt, "packerIt");
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
    }
  }


  private final String multilineEscape(final String data) {
    return data.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
  }
}

