package ro.isdc.wro.extensions.processor.support.hoganjs;

import org.mozilla.javascript.ScriptableObject;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.WroUtil;

import java.io.IOException;
import java.io.InputStream;


/**
 * Hogan.js: http://twitter.github.com/hogan.js/
 *
 * @author Eivind Barstad Waaler
 * @created 10 May 2012
 */
public class HoganJs {
  private static final String DEFAULT_HOGAN_JS = "hogan-2.0.0.min.js";

  private ScriptableObject scope;

  public String compile(final String content) {
    final RhinoScriptBuilder builder = initScriptBuilder();
    final String compileScript = String.format("Hogan.compile(%s, {asString: true});", WroUtil.toJSMultiLineString(content));
    return (String) builder.evaluate(compileScript, "Hogan.compile");
  }

  protected InputStream getHoganJsAsStream() {
    return HoganJs.class.getResourceAsStream(DEFAULT_HOGAN_JS);
  }

  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder;
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().evaluateChain(getHoganJsAsStream(), DEFAULT_HOGAN_JS);
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
