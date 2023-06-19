package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.FilenameUtils;

import ro.isdc.wro.extensions.processor.support.dustjs.DustJs;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * A processor for dustJs template framework. Uses <a href="https://github.com/linkedin/dustjs">dustjs</a> library to
 * transform a template into plain javascript.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 */
@SupportedResourceType(ResourceType.JS)
public class DustJsProcessor extends JsTemplateCompilerProcessor {
  public static final String ALIAS = "dustJs";

  @Override
  protected AbstractJsTemplateCompiler createCompiler() {
    return new DustJs();
  }

  @Override
  protected String getArgument(final Resource resource) {
    final String name = resource == null ? "" : FilenameUtils.getBaseName(resource.getUri());
    return String.format("'%s'", name);
  }
}
