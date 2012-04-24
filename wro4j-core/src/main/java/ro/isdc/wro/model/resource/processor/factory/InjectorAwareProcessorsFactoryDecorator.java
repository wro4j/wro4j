package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 *  Responsible for injecting each processor with required fields before being used.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public final class InjectorAwareProcessorsFactoryDecorator
    extends ProcessorsFactoryDecorator {
  private final Injector injector;
  
  
  public InjectorAwareProcessorsFactoryDecorator(final ProcessorsFactory decorated, final Injector injector) {
    super(decorated);
    Validate.notNull(injector);
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ResourcePreProcessor> getPreProcessors() {
    final Collection<ResourcePreProcessor> processors = super.getPreProcessors();
    for (ResourcePreProcessor processor : processors) {
      injector.inject(processor);
    }
    return processors;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ResourcePostProcessor> getPostProcessors() {
    final Collection<ResourcePostProcessor> processors = super.getPostProcessors();
    for (ResourcePostProcessor processor : processors) {
      injector.inject(processor);
    }
    return processors;
  }
}
