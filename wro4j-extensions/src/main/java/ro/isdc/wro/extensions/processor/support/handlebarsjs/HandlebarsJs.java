package ro.isdc.wro.extensions.processor.support.handlebarsjs;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.ScriptableObject;

import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.WroUtil;


/**
 * HandlebarsJS is a JavaScript templating engine which provides the power necessary to let you build semantic templates
 * effectively with no frustration.
 *
 * @author heldeen
 */
public class HandlebarsJs {

  /**
   * visible for testing, the init of a HandlebarsJs template
   */
  public static final String HANDLEBARS_JS_TEMPLATES_INIT = "(function() { var template = Handlebars.template, "
      + "templates = Handlebars.templates = Handlebars.templates || {};";

  private static final String DEFAULT_HANDLEBARS_JS = "handlebars-1.0.0.beta.6.js";
  private ScriptableObject scope;

  public String compile(final String content, final String name) {
    final RhinoScriptBuilder builder = initScriptBuilder();
    final String compileScript = String.format("Handlebars.precompile(%s);", WroUtil.toJSMultiLineString(content));
    return HANDLEBARS_JS_TEMPLATES_INIT + "templates['" + name + "'] = template("
        + (String) builder.evaluate(compileScript, "Handlebars.precompile") + " ); })();";
  }

  protected InputStream getHandlebarsJsAsStream() {
    return HandlebarsJs.class.getResourceAsStream(DEFAULT_HANDLEBARS_JS);
  }

  private RhinoScriptBuilder initScriptBuilder() {
    try {
      RhinoScriptBuilder builder;
      if (scope == null) {
        builder = RhinoScriptBuilder.newChain().evaluateChain(getHandlebarsJsAsStream(), DEFAULT_HANDLEBARS_JS);
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
