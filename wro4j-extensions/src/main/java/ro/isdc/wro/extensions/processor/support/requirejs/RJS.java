/*
 *  Copyright 2010 Alex Objelean.
 */
package ro.isdc.wro.extensions.processor.support.requirejs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;


/**
 * A wrapper around r.js, a commandline tool for combining JavaScript scripts that use the
 * Asynchronous Module Definition API (AMD) for declaring and using JavaScript modules and
 * regular JavaScript script files.
 * 
 */
public class RJS {
  private static final Logger LOG = LoggerFactory.getLogger(RJS.class);
  private static final String DEFAULT_SCRIPT = "r.js";


  /**
   * Override this method to use a different version of the script. This method is useful for upgrading the
   * processor independently of wro4j.
   *
   */
  protected InputStream getScriptAsStream() {
    return RJS.class.getResourceAsStream(DEFAULT_SCRIPT);
  }

  /**
   * @param args string arguments to pass to r.js invocation
   */
  public void compile(Object[] args) throws IOException {
    final StopWatch watch = new StopWatch();
    watch.start("compile");
    try {
      Context context = Context.enter();
      Global global = new Global();
      global.init(context);
      Scriptable argsObj = context.newArray(global, args);
      global.defineProperty("arguments", argsObj, ScriptableObject.DONTENUM);
      context.evaluateReader(global, new InputStreamReader(getScriptAsStream()), "r.js", 1, null);
      Context.exit();
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e));
    } finally {
      watch.stop();
      LOG.debug(watch.prettyPrint());
    }
  }
}
