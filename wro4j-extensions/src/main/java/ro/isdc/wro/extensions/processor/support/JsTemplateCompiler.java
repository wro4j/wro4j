package ro.isdc.wro.extensions.processor.support;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.ScriptableObject;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.WroUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class JsTemplateCompiler {
  private ScriptableObject scope;

  public String compile(final String content, final String optionalArgument) {
    final RhinoScriptBuilder builder = initScriptBuilder();
    final String argStr = createArgStr(optionalArgument) + createArgStr(getArguments());
    final String compileScript =
      String.format("%s(%s%s);", getCompileCommand(), WroUtil.toJSMultiLineString(content), argStr);
    return (String) builder.evaluate(compileScript, getCompileCommand());
  }

  protected abstract String getCompilerPath();

  protected abstract String getCompileCommand();

  protected String getArguments() {
    return null;
  }

  private String createArgStr(String argument) {
    return StringUtils.isNotBlank(argument) ? ", " + argument : "";
  }

  private InputStream getCompilerJsAsStream() {
    return this.getClass().getResourceAsStream(getCompilerPath());
  }

  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder;
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().evaluateChain(getCompilerJsAsStream(), getCompilerPath());
        scope = builder.getScope();
      } else {
        builder = RhinoScriptBuilder.newChain(scope);
      }
      return builder;
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading init script", ex);
    }
  }
}
