package ro.isdc.wro.extensions.processor.support.dustjs;

import org.mozilla.javascript.ScriptableObject;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.WroUtil;

import java.io.IOException;
import java.io.InputStream;

public class DustJs {
  private static final String DEFAULT_DUST_JS = "dust-full-0.3.0.min.js";

  private ScriptableObject scope;

  public String compile(String content, String name) {
    final RhinoScriptBuilder builder = initScriptBuilder();
    final String compileScript = String.format("dust.compile(%s, '%s');", WroUtil.toJSMultiLineString(content), name);
    return (String) builder.evaluate(compileScript, "dust.compile");
  }

  protected InputStream getDustJsAsStream() {
    return DustJs.class.getResourceAsStream(DEFAULT_DUST_JS);
  }

  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder;
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().evaluateChain(getDustJsAsStream(), DEFAULT_DUST_JS);
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
