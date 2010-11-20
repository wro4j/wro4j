/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Decorator for {@link ProcessorsFactory} responsible for processing @Inject annotations of processors provided by
 * decorated factory.
 *
 * @author Alex Objelean
 * @created 21 Nov 2010
 */
public class InjectorProcessorsFactoryDecorator
  implements ProcessorsFactory {
  private ProcessorsFactory decorated;
  private Injector injector;


  public InjectorProcessorsFactoryDecorator(final ProcessorsFactory decorated, final Injector injector) {
    this.decorated = decorated;
    this.injector = injector;
    scanPreProcessors();
    scanPostProcessors();
  }


  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePreProcessor> getPreProcessors() {
    scanPreProcessors();
    return decorated.getPreProcessors();
  }


  /**
   * Scan all preProcessors of decorated factory.
   */
  protected void scanPreProcessors() {
    // TODO ensure that it is not called to often
    for (final ResourcePreProcessor processor : decorated.getPreProcessors()) {
      injector.inject(processor);
    }
  }


  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePostProcessor> getPostProcessors() {
    scanPostProcessors();
    return decorated.getPostProcessors();
  }


  protected void scanPostProcessors() {
    // TODO ensure that it is not called to often
    for (final ResourcePostProcessor processor : decorated.getPostProcessors()) {
      injector.inject(processor);
    }
  }

}
