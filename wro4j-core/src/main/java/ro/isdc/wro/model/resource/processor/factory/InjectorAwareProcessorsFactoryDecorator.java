package ro.isdc.wro.model.resource.processor.factory;

import java.util.ArrayList;
import java.util.Collection;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorAwareDecorator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.MinimizeAwareProcessorDecorator;

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
    final Collection<ResourcePreProcessor> decoratedProcessors = new ArrayList<ResourcePreProcessor>();
    for (ResourcePreProcessor processor : processors) {
      final ResourcePreProcessor decoratedProcessor = new MinimizeAwareProcessorDecorator(processor);
      getInjector().inject(decoratedProcessor);
      decoratedProcessors.add(decoratedProcessor);
    }
    return decoratedProcessors;
  }
  
  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePostProcessor> getPostProcessors() {
    final Collection<ResourcePostProcessor> processors = getDecoratedObject().getPostProcessors();
    final Collection<ResourcePostProcessor> decoratedProcessors = new ArrayList<ResourcePostProcessor>();
    for (ResourcePostProcessor processor : processors) {
      final ResourcePostProcessor decoratedProcessor = new MinimizeAwareProcessorDecorator(processor);
      getInjector().inject(decoratedProcessor);
      decoratedProcessors.add(decoratedProcessor);
    }
    return processors;
  }
}
