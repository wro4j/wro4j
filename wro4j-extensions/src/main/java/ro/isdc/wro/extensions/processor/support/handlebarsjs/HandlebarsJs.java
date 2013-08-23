package ro.isdc.wro.extensions.processor.support.handlebarsjs;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;


/**
 * HandlebarsJS is a JavaScript templating engine which provides the power necessary to let you build semantic templates
 * effectively with no frustration.
 *
 * @author heldeen
 */
public class HandlebarsJs extends AbstractJsTemplateCompiler {
  private static final String HANDLEBARS_JS_TEMPLATES_INIT = "(function() { var template = Handlebars.template, "
      + "templates = Handlebars.templates = Handlebars.templates || {};";

  private WebjarUriLocator webjarLocator;

  /**
   * {@inheritDoc}
   */
  @Override
  public String compile(final String content, final String name) {
    return HANDLEBARS_JS_TEMPLATES_INIT + "templates[" + name + "] = template("
        + super.compile(content, "") + " ); })();";
  }

  /**
   * @return {@link WebjarUriLocator} instance to retrieve webjars.
   */
  private WebjarUriLocator getWebjarLocator() {
    if (webjarLocator == null) {
      webjarLocator = new WebjarUriLocator();
    }
    return webjarLocator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCompileCommand() {
    return "Handlebars.precompile";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected InputStream getCompilerAsStream() throws IOException {
    return getWebjarLocator().locate(WebjarUriLocator.createUri("handlebars.js"));
  }
}
