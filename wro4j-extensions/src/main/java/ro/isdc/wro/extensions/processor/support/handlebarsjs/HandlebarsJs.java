package ro.isdc.wro.extensions.processor.support.handlebarsjs;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.extensions.locator.WebjarResourceLocator;
import ro.isdc.wro.extensions.locator.WebjarResourceLocatorFactory;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;


/**
 * HandlebarsJS is a JavaScript templating engine which provides the power necessary to let you build semantic templates
 * effectively with no frustration.
 *
 * @author heldeen
 */
public class HandlebarsJs extends AbstractJsTemplateCompiler {
  private static final String HANDLEBARS_JS_TEMPLATES_INIT = "(function() { var template = Handlebars.template, "
      + "templates = Handlebars.templates = Handlebars.templates || {};";

  private ResourceLocatorFactory webjarLocatorFactory;

  /**
   * {@inheritDoc}
   */
  @Override
  public String compile(final String content, final String name) {
    return HANDLEBARS_JS_TEMPLATES_INIT + "templates[" + name + "] = template("
        + super.compile(content, "") + " ); })();";
  }

  /**
   * @return {@link ResourceLocatorFactory} instance to retrieve webjars.
   */
  private ResourceLocatorFactory getWebjarLocatorFactory() {
    if (webjarLocatorFactory == null) {
      webjarLocatorFactory = new WebjarResourceLocatorFactory();
    }
    return webjarLocatorFactory;
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
    return getWebjarLocatorFactory().locate(WebjarResourceLocator.createUri("handlebars.js"));
  }
}
