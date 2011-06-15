/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.factory;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Default implementation of processors factory. Holds processors in an array list and provide methods which allow
 * adding single processors or a collection of processors.
 *
 * @author Alex Objelean
 * @created 20 Nov 2010
 */
public class SimpleProcessorsFactory
  implements ProcessorsFactory {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleProcessorsFactory.class);
  /**
   * a list of pre processors.
   */
  private final Collection<ResourceProcessor> preProcessors = new ArrayList<ResourceProcessor>();
  /**
   * a list of post processors.
   */
  private final Collection<ResourceProcessor> postProcessors = new ArrayList<ResourceProcessor>();


  /**
   * {@inheritDoc}
   */
  public Collection<ResourceProcessor> getPreProcessors() {
    return preProcessors;
  }


  /**
   * {@inheritDoc}
   */
  public Collection<ResourceProcessor> getPostProcessors() {
    return postProcessors;
  }


  /**
   * {@inheritDoc}
   */
  public void setResourcePreProcessors(final Collection<ResourceProcessor> processors) {
    preProcessors.clear();
    if (processors != null) {
      preProcessors.addAll(processors);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void setResourcePostProcessors(final Collection<ResourceProcessor> processors) {
    postProcessors.clear();
    if (processors != null) {
      postProcessors.addAll(processors);
    }
  }


  /**
   * Add a {@link ResourceProcessor}.
   */
  public SimpleProcessorsFactory addPreProcessor(final ResourceProcessor processor) {
    preProcessors.add(processor);
    return this;
  }


  /**
   * Add a {@link ResourcePostProcessor}.
   */
  public SimpleProcessorsFactory addPostProcessor(final ResourceProcessor processor) {
    if (processor.getClass().isAnnotationPresent(Minimize.class)) {
      //TODO move large messages to properties file
      LOG.warn("It is recommended to add minimize aware processors to " +
          "pre processors instead of post processor, otherwise you " +
          "won't be able to disable minimization on specific resources " +
          "using minimize='false' attribute.");
    }
    postProcessors.add(processor);
    return this;
  }
}
