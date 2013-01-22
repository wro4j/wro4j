package ro.isdc.wro.extensions.processor.support.handlebarsjs;

import java.io.InputStream;

import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;


/**
 * HandlebarsJS is a JavaScript templating engine which provides the power necessary to let you build semantic templates
 * effectively with no frustration.
 *
 * @author heldeen
 */
public class HandlebarsJs extends AbstractJsTemplateCompiler {

  /**
   * visible for testing, the init of a HandlebarsJs template
   */
  public static final String HANDLEBARS_JS_TEMPLATES_INIT = "(function() { var template = Handlebars.template, "
      + "templates = Handlebars.templates = Handlebars.templates || {};";

  private static final String DEFAULT_HANDLEBARS_JS = "handlebars-1.0.rc.2.js";

  @Override
  public String compile(final String content, final String name) {

    return HANDLEBARS_JS_TEMPLATES_INIT + "templates[" + name + "] = template("
        + super.compile(content, "") + " ); })();";
  }

  @Override
  protected String getCompileCommand() {
    return "Handlebars.precompile";
  }

  @Override
  protected InputStream getCompilerAsStream() {
    return HandlebarsJs.class.getResourceAsStream(DEFAULT_HANDLEBARS_JS);
  }
}
