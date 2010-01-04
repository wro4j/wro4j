/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.util.List;

import ro.isdc.wro.model.Group;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * Performs the processing of a group of resources by type.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface GroupsProcessor {
  /**
   * Process a list of groups by type.
   *
   * @param groups
   *          to process.
   * @param type
   *          to process.
   * @return processed content as string.
   */
  String process(final List<Group> groups, final ResourceType type);

  /**
   * @param uriLocatorFactory {@link UriLocatorFactory} to set.
   */
  void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory);

  /**
   * Provide a list of preProcessors to apply on any type of content.
   *
   * @param processors
   *          a list of {@link ResourcePreProcessor} processors.
   */
  void setResourcePreProcessors(final List<ResourcePreProcessor> processors);

  /**
   * Provide a list of postProcessors to apply on any type of content.
   *
   * @param processors
   *          a list of {@link ResourcePostProcessor} processors.
   */
  void setResourcePostProcessors(final List<ResourcePostProcessor> processors);

  /**
   * Add a single css preProcessor to the chain of postProcessors.
   *
   * @param processor {@link ResourcePostProcessor} to add.
   */
  void addPostProcessor(final ResourcePostProcessor processor);

	/**
	 * Add a single preProcessor to the chain of preProcessors.
	 *
	 * @param processor {@link ResourcePostProcessor} to add.
	 */
	void addPreProcessor(final ResourcePreProcessor processor);

  /**
   * Search for the first available preprocessor of given type.
   *
   * @param processorClass to search in list of available preprocessors.
   * @return {@link ResourcePreProcessor} instance if any found, or null if such processor doesn't exist.
   */
	<T extends ResourcePreProcessor> T findPreProcessorByClass(final Class<T> processorClass);

}
