/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ro.isdc.wro.annot.SupportedResourceType;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * Implements basic methods. Specialized classes should inherit this class instead of interface.
 *
 * @author Alex Objelean
 * @created Created on Nov 26, 2008
 */
public abstract class AbstractGroupsProcessor implements GroupsProcessor {
  /**
   * a list of pre processors.
   */
  private final List<ResourcePreProcessor> preProcessors = new ArrayList<ResourcePreProcessor>();
  /**
   * a list of post processors.
   */
  private final List<ResourcePostProcessor> postProcessors = new ArrayList<ResourcePostProcessor>();
  /**
   * Used to get stream of the resources.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends ResourcePreProcessor> T findPreProcessorByClass(final Class<T> processorClass) {
    T found = null;
    final Set<ResourcePreProcessor> allPreProcessors = new HashSet<ResourcePreProcessor>();
    allPreProcessors.addAll(preProcessors);
    for (final ResourcePreProcessor processor : allPreProcessors) {
      if (processorClass.isInstance(processor)) {
        found = (T) processor;
        return found;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public final void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
  }

  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return this.uriLocatorFactory;
  }

  /**
   * @param type of resource for which you want to apply preProcessors or null if it doesn't matter (any resource).
   * @return a list of {@link ResourcePreProcessor} by provided type.
   */
  protected final List<ResourcePreProcessor> getPreProcessorsByType(final ResourceType type) {
    return getProcessorsByType(type, preProcessors);
  }

  /**
   * @param type of resource for which you want to apply postProcessors or null if it doesn't matter (any resource).
   * @return a list of {@link ResourcePostProcessor} by provided type.
   */
  protected final List<ResourcePostProcessor> getPostProcessorsByType(final ResourceType type) {
    return getProcessorsByType(type, postProcessors);
  }

  /**
   * @param <T> processor type. Can be {@link ResourcePreProcessor} or {@link ResourcePostProcessor}.
   * @param type {@link ResourceType} to apply for searching on available processors.
   * @param availableProcessors a list where to perform the search.
   * @return a list of found processors which satisfy the search criteria.
   */
  private <T> List<T> getProcessorsByType(final ResourceType type, final List<T> availableProcessors) {
    final List<T> found = new ArrayList<T>();
    for (final T processor : availableProcessors) {
      final SupportedResourceType supportedType = processor.getClass().getAnnotation(SupportedResourceType.class);
      final boolean isAnyTypeSatisfied = type == null && supportedType == null;
      final boolean isTypeSatisfied = type != null && supportedType != null && type == supportedType.type();
      if (isAnyTypeSatisfied || isTypeSatisfied) {
        found.add(processor);
      }
    }
    return found;
  }

  /**
   * {@inheritDoc}
   */
  public void setResourcePreProcessors(final Collection<ResourcePreProcessor> processors) {
    preProcessors.clear();
    preProcessors.addAll(processors);
  }

  /**
   * {@inheritDoc}
   */
  public void setResourcePostProcessors(final Collection<ResourcePostProcessor> processors) {
    postProcessors.clear();
    postProcessors.addAll(processors);
  }

	/**
   * Add a {@link ResourcePreProcessor}.
   */
  public void addPreProcessor(final ResourcePreProcessor processor) {
    preProcessors.add(processor);
  }

  /**
   * Add a {@link ResourcePostProcessor}.
   */
  public void addPostProcessor(final ResourcePostProcessor processor) {
    postProcessors.add(processor);
  }
}
