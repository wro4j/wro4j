/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Decorates {@link ProcessorsFactory}.
 *
 * @author Alex Objelean
 * @created 22 Nov 2010
 */
public class ProcessorsFactoryDecorator
  implements ProcessorsFactory {
  private final ProcessorsFactory decorated;

  public ProcessorsFactoryDecorator(final ProcessorsFactory decorated) {
    if (decorated == null) {
      throw new IllegalArgumentException("processorsFactory cannot be null!");
    }
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
