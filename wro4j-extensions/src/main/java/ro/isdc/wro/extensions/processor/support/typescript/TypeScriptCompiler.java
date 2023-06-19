package ro.isdc.wro.extensions.processor.support.typescript;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Uses rhino to compile a TypeScript into javascript.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public class TypeScriptCompiler {
  private static final String PARAM_ERRORS = "errors";
  private static final String PARAM_SOURCE = "source";
  private static final Logger LOG = LoggerFactory.getLogger(TypeScriptCompiler.class);
  private static final String TYPESCRIPT_JS = "typescript-0.8.js";
  private static final String TYPESCRIPT_COMPILE_JS = "typescript.compile-0.3.js";
  private final String ecmaScriptVersion = "TypeScript.CodeGenTarget.ES5";
  private ScriptableObject scope;

  public String compile(final String typeScript) {

    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("compile");
    try {
      final String execute = getCompilationCommand(typeScript);
      final NativeObject compilationResult = (NativeObject) builder.evaluate(execute, "compile");
      final NativeArray errors = (NativeArray) compilationResult.get(PARAM_ERRORS, scope);
      if (errors.getLength() > 0) {
        throwCompilationError(errors);
      }
      return compilationResult.get(PARAM_SOURCE, scope).toString();
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }

  private void throwCompilationError(final NativeArray errors) {
    final StringBuilder sb = new StringBuilder();
    for (final Object error : errors.getAllIds()) {
      sb.append(error.toString()).append("\n");
    }
    LOG.debug("Compilation errors: {}", sb);
    throw new WroRuntimeException(sb.toString());
  }

  /**
   * Creates compilation command for provided typescript input.
   *
   * @param input
   *          the TypeScript to compile into js.
   * @return compilation command.
   */
  private String getCompilationCommand(final String input) {
    return String.format("%s %s", WroUtil.toJSMultiLineString(input),
        ecmaScriptVersion);
  }

  /**
   * @return the stream of the compiler resource (javascript) used to compile templates.
   * @throws IOException
   *           if the stream could not be located.
   */
  private InputStream getCompilerAsStream()
      throws IOException {
    return new SequenceInputStream(getCompilerStream(), getCompilerWrapperStream());
  }

  /**
   * @return the stream for the script performing actual compilation wrapper.
   */
  protected InputStream getCompilerWrapperStream() {
    return TypeScriptCompiler.class.getResourceAsStream(TYPESCRIPT_COMPILE_JS);
  }

  /**
   * @return the stream for the script for TypeScript compiler.
   */
  protected InputStream getCompilerStream() {
    return TypeScriptCompiler.class.getResourceAsStream(TYPESCRIPT_JS);
  }

  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder;
      if (scope == null) {
        builder = RhinoScriptBuilder.newClientSideAwareChain().evaluateChain(getCompilerAsStream(), "templateCompiler.js");
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException ex) {
      throw new WroRuntimeException("Failed reading init script", ex);
    }
  }
}
