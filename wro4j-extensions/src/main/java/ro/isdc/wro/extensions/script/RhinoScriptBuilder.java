/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


/**
 * Used to evaluate javascript on the serverside using rhino javascript engine.
 *
 * @author Alex Objelean
 */
public class RhinoScriptBuilder {
  private Context context;
  private Scriptable scope;

  /**
   * Constroctor.
   */
  private RhinoScriptBuilder() {
    initContext();
  }

  /**
   * Initialize the context.
   */
  private void initContext() {
    context = Context.enter();
    context.setOptimizationLevel(-1);
    context.setLanguageVersion(Context.VERSION_1_7);
    scope = context.initStandardObjects();

    final String printFunction = "function print(message) {java.lang.System.out.println(message);}";
    context.evaluateString(scope, printFunction, "print", 1, null);
  }

  /**
   * Add a clinet side environment to the script context (client-side aware).
   * @return {@link RhinoScriptBuilder} used to chain evaluation of the scripts.
   * @throws IOException
   */
  public RhinoScriptBuilder addClientSideEnvironment() {
    try {
      final String SCRIPT_ENV = "env.rhino.js";
      final InputStream script = RhinoScriptBuilder.class.getResourceAsStream(SCRIPT_ENV);
      evaluateScript(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize env.rhino script", e);
    }
  }

  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param script {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final InputStream script, final String sourceName) throws IOException {
    context.evaluateReader(scope, new InputStreamReader(script), sourceName, 1, null);
    return this;
  }

  /**
   * Evaluates a script from a stream.
   *
   * @param script {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final InputStream script, final String sourceName) throws IOException {
    try {
      return evaluateScript(script, sourceName);
    } finally {
      Context.exit();
    }
  }

  /**
   * Evaluates the script without exiting the Context.
   * @param script
   * @param sourceName
   * @return
   * @throws IOException
   */
  private Object evaluateScript(final InputStream script, final String sourceName)
    throws IOException {
    return context.evaluateReader(scope, new InputStreamReader(script), sourceName, 1, null);
  }

  /**
   * Evaluates a script from a reader.
   *
   * @param script {@link Reader} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final Reader script, final String sourceName) throws IOException {
    try {
      return context.evaluateReader(scope, script, sourceName, 1, null);
    } finally {
      Context.exit();
    }
  }

  /**
   * Evaluates a script.
   *
   * @param script string representation of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final String script, final String sourceName) {
    try {
      return context.evaluateString(scope, script, sourceName, 1, null);
    } finally {
      Context.exit();
    }
  }

  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newChain() {
    return new RhinoScriptBuilder();
  }

  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newClientSideAwareChain() {
    return new RhinoScriptBuilder().addClientSideEnvironment();
  }
}
