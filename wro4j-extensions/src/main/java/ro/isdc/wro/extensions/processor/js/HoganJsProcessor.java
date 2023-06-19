package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.hoganjs.HoganJs;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * A processor for hogan.js template framework. Uses <a href="http://twitter.github.com/hogan.js/">hogan.js</a> library to
 * transform a template into plain javascript.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 */
@SupportedResourceType(ResourceType.JS)
public class HoganJsProcessor extends JsTemplateCompilerProcessor {
  public static final String ALIAS = "hoganJs";

  @Override
  protected AbstractJsTemplateCompiler createCompiler() {
    return new HoganJs();
  }
}
