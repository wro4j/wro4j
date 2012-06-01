package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.handlebarsjs.HandlebarsJs;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;


/**
 * Compiles HandlebarsJS templates to javascript.
 *
 * @author heldeen
 * @since 1.4.7
 */
@SupportedResourceType(ResourceType.JS)
public class HandlebarsJsProcessor
    extends JsTemplateCompilerProcessor {
  public static final String ALIAS = "handlebarsJs";


  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractJsTemplateCompiler createCompiler() {
    return new HandlebarsJs();
  }
}
