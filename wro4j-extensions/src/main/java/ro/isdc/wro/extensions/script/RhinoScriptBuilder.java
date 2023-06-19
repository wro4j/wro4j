/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.script;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.ExternalLibrary;
import ro.isdc.wro.extensions.locator.WebjarUriLocator;

/**
 * Used to evaluate javascript on the serverside using rhino javascript engine.
 * Encapsulate and hides all implementation details used by rhino to evaluate
 * javascript on the serverside.
 *
 * @author Alex Objelean
 */
public final class RhinoScriptBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RhinoScriptBuilder.class);

	private static final String SCRIPT_COMMONS = "commons.js";
	private static final String SCRIPT_ENVIRONMENT = "env.rhino.js";
	private static final String SCRIPT_JSON = "json2.min.js";
	private static final String SCRIPT_CYCLE = "cycle.js";

	private final ScriptableObject scope;

	private RhinoScriptBuilder() {
		this(null);
	}

	private RhinoScriptBuilder(final ScriptableObject scope) {
		this.scope = createContext(scope);
	}

	private Context getContext() {
		initContext();
		return Context.getCurrentContext();
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

		final Context context = getContext();
		context.setOptimizationLevel(-1);
		// TODO redirect errors from System.err to LOG.error()
		context.setErrorReporter(new ToolErrorReporter(false));
		context.setLanguageVersion(Context.VERSION_ES6);
		final ScriptableObject scriptCommon = (ScriptableObject) context.initStandardObjects(initialScope);

		try (InputStream script = new AutoCloseInputStream(getClass().getResourceAsStream(SCRIPT_COMMONS))) {
			context.evaluateReader(scriptCommon, new InputStreamReader(script), SCRIPT_COMMONS, 1, null);
		} catch (final IOException e) {
			throw new RuntimeException("Problem while evaluationg commons script.", e);
		}

		return scriptCommon;
	}

  /**
   * Add a client side environment to the script context (client-side aware).
   *
   * @return {@link RhinoScriptBuilder} used to chain evaluation of the scripts.
   */
  public RhinoScriptBuilder addClientSideEnvironment() {
    try {
      //final InputStream scriptEnv = getClass().getResourceAsStream(SCRIPT_ENV);
      final InputStream scriptEnv = new WebjarUriLocator().locate(SCRIPT_ENVIRONMENT);
      evaluateChain(scriptEnv, SCRIPT_ENVIRONMENT);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize env.rhino script", e);
    }
  }

	/**
	 * This method will load JSON utility and aslo a Douglas Crockford's <a href=
	 * "https://github.com/douglascrockford/JSON-js/blob/master/cycle.js">utility</a>
	 * required for decycling objects which would fail otherwise when using
	 * JSON.stringify.
	 */
	public RhinoScriptBuilder addJSON() {
		try {
			final InputStream script = new AutoCloseInputStream(
					new WebjarUriLocator().locate(WebjarUriLocator.createUri(SCRIPT_JSON)));
			final InputStream scriptCycle = getClass().getResourceAsStream(SCRIPT_CYCLE);

			evaluateChain(script, SCRIPT_JSON);
			evaluateChain(scriptCycle, SCRIPT_CYCLE);
			return this;
		} catch (final IOException e) {
			throw new RuntimeException("Couldn't initialize " + SCRIPT_JSON + " script", e);
		}
	}

	/**
	 * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script
	 * evaluation.
	 *
	 * @param stream     {@link InputStream} of the script to evaluate.
	 * @param sourceName the name of the evaluated script.
	 * @return {@link RhinoScriptBuilder} chain with required script evaluated.
	 * @throws IOException if the script couldn't be retrieved.
	 */
	public RhinoScriptBuilder evaluateChain(final InputStream stream, final String sourceName) throws IOException {
		notNull(stream);
		try (stream) {
			getContext().evaluateReader(scope, new InputStreamReader(stream), sourceName, 1, null);
			return this;
		} catch (final RhinoException e) {
			LOG.error("RhinoException: {}", RhinoUtils.createExceptionMessage(e));
			throw e;
		} catch (final RuntimeException e) {
			LOG.error("Exception caught", e);
			throw e;
		}
	}

	/**
	 * Makes sure the context is properly initialized.
	 */
	private void initContext() {
		if (Context.getCurrentContext() == null) {
			Context.enter();
		}
	}

	/**
	 * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script
	 * evaluation.
	 *
	 * @param script     the string representation of the script to evaluate.
	 * @param sourceName the name of the evaluated script.
	 * @return evaluated object.
	 */
	public RhinoScriptBuilder evaluateChain(final String script, final String sourceName) {
		notNull(script);
		getContext().evaluateString(scope, script, sourceName, 1, null);
		return this;
	}

	/**
	 * Evaluates a script from a reader.
	 *
	 * @param reader     {@link Reader} of the script to evaluate.
	 * @param sourceName the name of the evaluated script.
	 * @return evaluated object.
	 * @throws IOException if the script couldn't be retrieved.
	 */
	public Object evaluate(final Reader reader, final String sourceName) throws IOException {
		notNull(reader);
		try {
			return evaluate(IOUtils.toString(reader), sourceName);
		} finally {
			reader.close();
		}
	}

	/**
	 * Evaluates a script.
	 *
	 * @param script     string representation of the script to evaluate.
	 * @param sourceName the name of the evaluated script.
	 * @return evaluated object.
	 */
	public Object evaluate(final String script, final String sourceName) {
		notNull(script);
		// make sure we have a context associated with current thread
		try {
			return getContext().evaluateString(scope, script, sourceName, 1, null);
		} catch (final RhinoException e) {
			final String message = RhinoUtils.createExceptionMessage(e);
			LOG.error("JavaScriptException occured: {}", message);
			throw new WroRuntimeException(message);
		} finally {
			// Rhino throws an exception when trying to exit twice. Make sure we don't get
			// any exception
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
