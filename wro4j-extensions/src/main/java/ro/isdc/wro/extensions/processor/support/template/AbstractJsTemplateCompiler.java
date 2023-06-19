package ro.isdc.wro.extensions.processor.support.template;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.ScriptableObject;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.WroUtil;


/**
 * A base class for template processors like: dustJS or hoganJS.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.7
 */
public abstract class AbstractJsTemplateCompiler {
  private ScriptableObject scope;

  /**
   * Compiles the javascript template into plain javascript.
   *
   * @param content
   *          the template to be compiled.
   * @param optionalArgument
   *          any additional arguments used by template script.
   * @return the compiled javascript.
   */
  public String compile(final String content, final String optionalArgument) {
    final RhinoScriptBuilder builder = initScriptBuilder();
    final String argStr = createArgStr(optionalArgument) + createArgStr(getArguments());
    final String compileScript =
      String.format("%s(%s%s);", getCompileCommand(), WroUtil.toJSMultiLineString(content), argStr);
    return (String) builder.evaluate(compileScript, getCompileCommand());
  }

  /**
   * @return the js statement used to execute the compilation of the template.
   */
  protected abstract String getCompileCommand();

  /**
   * @return additional arguments for the compiler.
   */
  protected String getArguments() {
    return null;
  }

  private String createArgStr(final String argument) {
    return StringUtils.isNotBlank(argument) ? ", " + argument : "";
  }

  /**
   * @return the stream of the compiler resource (javascript) used to compile templates.
   * @throws IOException
   *           if the stream could not be located.
   */
  protected abstract InputStream getCompilerAsStream() throws IOException;

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
