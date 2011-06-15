/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collection;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


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
  public Collection<ResourceProcessor> getPreProcessors() {
    return decorated.getPreProcessors();
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ResourceProcessor> getPostProcessors() {
    return decorated.getPostProcessors();
  }
}
