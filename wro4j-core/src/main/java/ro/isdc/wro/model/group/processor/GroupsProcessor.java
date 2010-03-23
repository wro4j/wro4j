/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.Serializable;
import java.util.Collection;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Performs the processing of a group based on resourceType. The implementation should take care of
 * {@link WroConfiguration#isMinimize()} to skip the processors which perform minimization of resources, otherwise
 * changing of minimize flag will have no effect.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface GroupsProcessor extends Serializable {
  /**
   * Process a collection of groups by type.
   *
   * @param groups to process.
   * @param type to process.
   * @param minimize if true, the result of the processing will be minimized
   * @return processed content as string.
   */
  String process(final Collection<Group> groups, final ResourceType type, final boolean minimize);

  /**
   * @param uriLocatorFactory {@link UriLocatorFactory} to set.
   */
  void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory);

  /**
   * @return {@link UriLocatorFactory} for this GroupsProcessor.
   */
  UriLocatorFactory getUriLocatorFactory();

  /**
   * Provide a list of preProcessors to apply on any type of content.
   *
   * @param processors
   *          a list of {@link ResourcePreProcessor} processors.
   */
  void setResourcePreProcessors(final Collection<ResourcePreProcessor> processors);

  /**
   * Provide a list of postProcessors to apply on any type of content.
   *
   * @param processors
   *          a list of {@link ResourcePostProcessor} processors.
   */
  void setResourcePostProcessors(final Collection<ResourcePostProcessor> processors);

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
