/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.algorithm.packer;

import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
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
      final ScriptEngineManager manager = new ScriptEngineManager();
      final List<ScriptEngineFactory> factories = getAvailableEngines(manager);
      // create JavaScript engine
      scriptEngine = manager.getEngineByName("js");
      if (scriptEngine == null) {
        throw new IllegalStateException("No ScriptManager for JavaScript is available. Available managers are: "
            + factories);
      }

      final String packagePath = WroUtil.toPackageAsFolder(getClass());
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      final String base2 = IOUtils.toString(classLoader.getResourceAsStream(packagePath + "/base2.js"));
      final String packer = IOUtils.toString(classLoader.getResourceAsStream(packagePath + "/packer.js"));
      scriptEngine.eval(base2);
      scriptEngine.eval(packer);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript packer.js", ex);
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
    }
  }


  private List<ScriptEngineFactory> getAvailableEngines(final ScriptEngineManager manager) {
    final List<ScriptEngineFactory> engines = manager.getEngineFactories();
    if (engines.isEmpty()) {
      LOG.debug("No scripting engines were found");
    } else {
      LOG.debug("The following " + engines.size() + " scripting engines were found");
      for (final ScriptEngineFactory engine : engines) {
        LOG.debug("Engine name: " + engine.getEngineName());
        LOG.debug("\tVersion: " + engine.getEngineVersion());
        LOG.debug("\tLanguage: " + engine.getLanguageName());
        final List<String> extensions = engine.getExtensions();
        if (extensions.size() > 0) {
          LOG.debug("\tEngine supports the following extensions:");
          for (final String e : extensions) {
            LOG.debug("\t\t" + e);
          }
        }
        final List<String> shortNames = engine.getNames();
        if (shortNames.size() > 0) {
          LOG.debug("\tEngine has the following short names:");
          for (final String n : engine.getNames()) {
            LOG.debug("\t\t" + n);
          }
        }
        LOG.debug("=========================");
      }
    }
    return engines;
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
