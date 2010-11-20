/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Default implementation of processors factory. Holds processors in an array list and provide methods which allow
 * adding single processors or a collection of processors.
 *
 * @author Alex Objelean
 * @created 20 Nov 2010
 */
public class SimpleProcessorsFactory
  implements ProcessorsFactory {
  /**
   * a list of pre processors.
   */
  private final Collection<ResourcePreProcessor> preProcessors = new ArrayList<ResourcePreProcessor>();
  /**
   * a list of post processors.
   */
  private final Collection<ResourcePostProcessor> postProcessors = new ArrayList<ResourcePostProcessor>();


  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePreProcessor> getPreProcessors() {
    return preProcessors;
  }


  /**
   * {@inheritDoc}
   */
  public Collection<ResourcePostProcessor> getPostProcessors() {
    return postProcessors;
  }


  /**
   * {@inheritDoc}
   */
  public void setResourcePreProcessors(final Collection<ResourcePreProcessor> processors) {
    preProcessors.clear();
    if (processors != null) {
      preProcessors.addAll(processors);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void setResourcePostProcessors(final Collection<ResourcePostProcessor> processors) {
    postProcessors.clear();
    if (processors != null) {
      postProcessors.addAll(processors);
    }
  }


  /**
   * Add a {@link ResourcePreProcessor}.
   */
  public SimpleProcessorsFactory addPreProcessor(final ResourcePreProcessor processor) {
    preProcessors.add(processor);
    return this;
  }


  /**
   * Add a {@link ResourcePostProcessor}.
   */
  public SimpleProcessorsFactory addPostProcessor(final ResourcePostProcessor processor) {
    postProcessors.add(processor);
    return this;
  }
}
