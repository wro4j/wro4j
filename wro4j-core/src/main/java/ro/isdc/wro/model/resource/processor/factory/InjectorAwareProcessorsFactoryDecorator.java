package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorAwareDecorator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Responsible for injecting each processor with required fields before being used.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public final class InjectorAwareProcessorsFactoryDecorator
    extends InjectorAwareDecorator<ProcessorsFactory> implements ProcessorsFactory {
  
  public InjectorAwareProcessorsFactoryDecorator(final ProcessorsFactory decorated, final Injector injector) {
    super(decorated, injector);
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePreProcessor> getPreProcessors() {
    final Collection<ResourcePreProcessor> processors = getDecoratedObject().getPreProcessors();
    for (ResourcePreProcessor processor : processors) {
      getInjector().inject(processor);
    }
    return processors;
  }
  
  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePostProcessor> getPostProcessors() {
    final Collection<ResourcePostProcessor> processors = getDecoratedObject().getPostProcessors();
    for (ResourcePostProcessor processor : processors) {
      getInjector().inject(processor);
    }
    return processors;
  }
}
