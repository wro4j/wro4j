package ro.isdc.wro.model.resource.processor.factory;

import java.util.Collection;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;

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
    inject(decorated);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ResourceProcessor> getPreProcessors() {
    final Collection<ResourceProcessor> processors = super.getPreProcessors();
    for (ResourceProcessor processor : processors) {
      inject(processor);
    }
    return processors;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ResourceProcessor> getPostProcessors() {
    final Collection<ResourceProcessor> processors = super.getPostProcessors();
    for (ResourceProcessor processor : processors) {
      inject(new ProcessorDecorator(processor));
    }
    return processors;
  }
  
  /**
   * Handles injection for decorators.
   */
  private void inject(final Object object) {
    injector.inject(object);
    if (object instanceof ProcessorDecorator) {
      injector.inject(((ProcessorDecorator) object).getDecoratedProcessor());
    }
    if (object instanceof ProcessorsFactoryDecorator) {
      injector.inject(((ProcessorsFactoryDecorator) object).getDecoratedObject());
    }
  }
}
