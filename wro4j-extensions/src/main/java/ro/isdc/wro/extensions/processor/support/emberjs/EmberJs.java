package ro.isdc.wro.extensions.processor.support.emberjs;

import java.io.InputStream;
import java.io.SequenceInputStream;

import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;


/**
 * EmberJS is a framework which provide a templating engine built on top of Handlebars.
 *
 * @author blemoine
 */
public class EmberJs
    extends AbstractJsTemplateCompiler {

  private static final String DEFAULT_HANDLEBARS_JS = "handlebars-1.0.rc.2.js";
  private static final String DEFAULT_EMBER_JS = "ember-1.0.0-pre.4.js";
  private static final String DEFAULT_HEADLESS_EMBER_JS = "headless-ember.js";

  /**
   * visible for testing, the init of a HandlebarsJs template
   */
  @Override
  public String compile(final String content, final String name) {
    return "(function() {Ember.TEMPLATES[" + name + "] = Ember.Handlebars.template(" + super.compile(content, "")
        + ")})();";
  }

  @Override
  protected String getCompileCommand() {
    // Function present in headless-ember
    return "precompileEmberHandlebars";
  }

  @Override
  protected InputStream getCompilerAsStream() {
    final InputStream handlebars = EmberJs.class.getResourceAsStream(DEFAULT_HANDLEBARS_JS);
    final InputStream headlessEmber = EmberJs.class.getResourceAsStream(DEFAULT_HEADLESS_EMBER_JS);
    final InputStream ember = EmberJs.class.getResourceAsStream(DEFAULT_EMBER_JS);
    return new SequenceInputStream(new SequenceInputStream(handlebars, headlessEmber), ember);
  }
}
