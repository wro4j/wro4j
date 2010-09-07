/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.packer;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Apply Packer compressor script using scriptEngine.
 *
 * @author Alex Objelean
 */
public class PackerJs {
  private static final Logger LOG = LoggerFactory.getLogger(PackerJs.class);
  private ScriptEngine scriptEngine;

  public PackerJs() {
    try {
      final ScriptEngineManager factory = new ScriptEngineManager();
      // create JavaScript engine
      scriptEngine = factory.getEngineByName("JavaScript");

      final String packagePath = WroUtil.toPackageAsFolder(getClass());
      final String base2 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(packagePath + "/base2.js"));
      final String packer = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(packagePath + "/packer.js"));
      scriptEngine.eval(base2);
      scriptEngine.eval(packer);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript packer.js", ex);
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
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
      watch.start("pack");
      final String script = multilineEscape(data);
      scriptEngine.eval("var scriptToPack = \"" + script + "\"");
      LOG.debug("Script to pack evaluated");
      final String packIt = "new Packer().pack(scriptToPack, true, true);";
      LOG.debug("evaluating packer script");
      final String result = scriptEngine.eval(packIt).toString();
      LOG.debug("packer result: " + result);
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return result;
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
    }
  }


  private final String multilineEscape(final String data) {
    return data.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
  }
}
