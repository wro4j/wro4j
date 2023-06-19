package ro.isdc.wro.extensions.processor.support.hoganjs;

import java.io.InputStream;

import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;


/**
 * Hogan.js: http://twitter.github.com/hogan.js/
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.7
 */
public class HoganJs
    extends AbstractJsTemplateCompiler {
  private static final String DEFAULT_HOGAN_JS = "hogan-2.0.0.min.js";
  /**
   * {@inheritDoc}
   */
  @Override
  public String compile(final String content, final String optionalArgument) {
    return String.format("Hogan.cache['%s'] = %s;", optionalArgument, super.compile(content, ""));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected InputStream getCompilerAsStream() {
    return HoganJs.class.getResourceAsStream(DEFAULT_HOGAN_JS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCompileCommand() {
    return "Hogan.compile";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getArguments() {
    return "{asString: true}";
  }
}
