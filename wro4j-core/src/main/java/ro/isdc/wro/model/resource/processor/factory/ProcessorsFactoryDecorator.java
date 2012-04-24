package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import org.apache.commons.lang3.Validate;

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
  private final ProcessorsFactory decorated;
  
  public ProcessorsFactoryDecorator(final ProcessorsFactory decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }
  
  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePreProcessor> getPreProcessors() {
    return decorated.getPreProcessors();
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePostProcessor> getPostProcessors() {
    return decorated.getPostProcessors();
  }
}
