/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to evaluate javascript on the serverside using rhino javascript engine. Encapsulate and hides all implementation
 * details used by rhino to evaluate javascript on the serverside.
 *
 * @author Alex Objelean
 */
public class RhinoScriptBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(RhinoScriptBuilder.class);
  private Context context;
  private final ScriptableObject scope;


  private RhinoScriptBuilder() {
    this(null);
  }


  private RhinoScriptBuilder(final ScriptableObject scope) {
    this.scope = createContext(scope);
  }


  /**
   * @return the context
   */
  public ScriptableObject getScope() {
    return this.scope;
  }


  /**
   * Initialize the context.
   */
  private ScriptableObject createContext(final ScriptableObject initialScope) {
    initContext();
    context.setOptimizationLevel(-1);
    // TODO redirect errors from System.err to LOG.error()
    context.setErrorReporter(new ToolErrorReporter(false));
    context.setLanguageVersion(Context.VERSION_1_8);
    InputStream script = null;
    final ScriptableObject scope = (ScriptableObject) context.initStandardObjects(initialScope);
    try {
      script = getClass().getResourceAsStream("commons.js");
      context.evaluateReader(scope, new InputStreamReader(script), "commons.js", 1, null);
    } catch (final IOException e) {
      throw new RuntimeException("Problem while evaluationg commons script.", e);
    } finally {
      IOUtils.closeQuietly(script);
    }
    return scope;
  }

  /**
   * Add a clinet side environment to the script context (client-side aware).
   *
   * @return {@link RhinoScriptBuilder} used to chain evaluation of the scripts.
   * @throws IOException
   */
  public RhinoScriptBuilder addClientSideEnvironment() {
    try {
      final String SCRIPT_ENV = "env.rhino.min.js";
      final InputStream script = getClass().getResourceAsStream(SCRIPT_ENV);
      evaluateChain(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize env.rhino script", e);
    }
  }


  public RhinoScriptBuilder addJSON() {
    try {
      final String SCRIPT_ENV = "json2.min.js";
      final InputStream script = getClass().getResourceAsStream(SCRIPT_ENV);
      evaluateChain(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize json2.min.js script", e);
    }
  }


  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param stream {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return {@link RhinoScriptBuilder} chain with required script evaluated.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final InputStream stream, final String sourceName)
    throws IOException {
    Validate.notNull(stream);
    initContext();
    try {
      context.evaluateReader(scope, new InputStreamReader(stream), sourceName, 1, null);
      return this;
    } catch (final RuntimeException e) {
      LOG.error("Exception caught", e);
      if (e instanceof RhinoException) {
        LOG.error("RhinoException: " + RhinoUtils.createExceptionMessage((RhinoException) e));
      }
      throw e;
    } finally {
      stream.close();
    }
  }


  public void initContext() {
    if (context == null) {
      context = Context.enter();
    }
  }


  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param script the string representation of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final String script, final String sourceName) {
    Validate.notNull(script);
    initContext();
    context.evaluateString(scope, script, sourceName, 1, null);
    return this;
  }


  /**
   * Evaluates a script from a reader.
   *
   * @param reader {@link Reader} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final Reader reader, final String sourceName)
    throws IOException {
    Validate.notNull(reader);
    try {
      return evaluate(IOUtils.toString(reader), sourceName);
    } finally {
      reader.close();
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
    Validate.notNull(script);
    // make sure we have a context associated with current thread
    initContext();
    try {
      return context.evaluateString(scope, script, sourceName, 1, null);
    } catch (final JavaScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw e;
    } finally {
      // Rhino throws an exception when trying to exit twice. Make sure we don't get any exception
      if (Context.getCurrentContext() != null) {
        Context.exit();
      }
    }
  }

  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newChain() {
    return new RhinoScriptBuilder();
  }


  public static RhinoScriptBuilder newChain(final ScriptableObject scope) {
    return new RhinoScriptBuilder(scope);
  }


  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newClientSideAwareChain() {
    return new RhinoScriptBuilder().addClientSideEnvironment();
  }
}
