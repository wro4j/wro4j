package ro.isdc.wro.extensions.processor.support.typescript;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import org.mozilla.javascript.ScriptableObject;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;


/**
 * Uses rhino to compile a TypeScript into javascript.
 *
 * @author Alex Objelean
 * @since 1.6.3
 * @created 18 Jan 2013
 */
public class TypeScriptCompiler {
  private static final String TYPESCRIPT_JS = "typescript-0.8.js";
  private static final String TYPESCRIPT_COMPILE_JS = "typescript.compile-0.3.js";
  private final String ecmaScriptVersion = "TypeScript.CodeGenTarget.ES5";
  private ScriptableObject scope;

  public String compile(final String typeScript) {
    return null;
  }

  private String getCompilationCommand() {
    return String.format("var compilationResult; compilationResult = compilerWrapper.compile(input, %s)",
        ecmaScriptVersion);
  }

  /**
   * @return the stream of the compiler resource (javascript) used to compile templates.
   * @throws IOException
   *           if the stream could not be located.
   */
  protected InputStream getCompilerAsStream()
      throws IOException {
    final InputStream typeScriptJs = TypeScriptCompiler.class.getResourceAsStream(TYPESCRIPT_JS);
    final InputStream typeScriptCompileJs = TypeScriptCompiler.class.getResourceAsStream(TYPESCRIPT_COMPILE_JS);
    return new SequenceInputStream(typeScriptJs, typeScriptCompileJs);
  }

  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder;
      if (scope == null) {
        builder = RhinoScriptBuilder.newClientSideAwareChain().evaluateChain(getCompilerAsStream(),
            "templateCompiler.js");
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
