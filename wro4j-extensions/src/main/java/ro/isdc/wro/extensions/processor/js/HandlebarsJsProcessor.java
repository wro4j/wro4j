/**
 *
 */
package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.handlebarsjs.HandlebarsJs;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * This {@link ResourcePreProcessor} compiles HandlebarsJS templates to javascript.
 *
 * @author heldeen
 */
@SupportedResourceType(ResourceType.JS)
public class HandlebarsJsProcessor
    extends JsTemplateCompilerProcessor {
  public static final String ALIAS = "HandlebarsJs";


  @Override
  protected AbstractJsTemplateCompiler createCompiler() {
    return new HandlebarsJs();
  }

}
