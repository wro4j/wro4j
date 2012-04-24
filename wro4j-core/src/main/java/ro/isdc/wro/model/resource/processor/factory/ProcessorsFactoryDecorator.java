package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Simple decorator of {@link ProcessorsFactory}.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */

public class ProcessorsFactoryDecorator
    implements ProcessorsFactory {
  
  public Collection<ResourcePreProcessor> getPreProcessors() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Collection<ResourcePostProcessor> getPostProcessors() {
    // TODO Auto-generated method stub
    return null;
  }
  
}
