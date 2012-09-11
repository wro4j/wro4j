package ro.isdc.wro.extensions.processor.css;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;


/**
 * Similar to {@link LessCssProcessor} but will prefer using {@link NodeLessCssProcessor} if it is supported and will
 * fallback to rhino based processor.
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 11 Sep 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class FallbackLessCssProcessor {
  
}
