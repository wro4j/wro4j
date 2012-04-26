package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.AbstractDecorator;

/**
 * Simple decorator of {@link ProcessorsFactory}.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public class ProcessorsFactoryDecorator extends AbstractDecorator<ProcessorsFactory>
    implements ProcessorsFactory {
  
  public ProcessorsFactoryDecorator(final ProcessorsFactory decorated) {
    super(decorated);
  }
  
  /**
   * {@inheritDoc}
   */
  public Collection<ResourceProcessor> getPreProcessors() {
    return getDecoratedObject().getPreProcessors();
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ResourceProcessor> getPostProcessors() {
    return getDecoratedObject().getPostProcessors();
  }
}
