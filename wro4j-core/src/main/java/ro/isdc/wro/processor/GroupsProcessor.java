/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.util.List;

import ro.isdc.wro.model.Group;
import ro.isdc.wro.resource.ResourceType;

/**
 * Performs the processing of a group of resources by type.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
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
   * Provide a list of preProcessors to apply on css content.
   * 
   * @param processors
   *          a list of {@link ResourcePreProcessor} processors.
   */
  void setCssPreProcessors(final List<ResourcePreProcessor> processors);

  /**
   * Provide a list of preProcessors to apply on js content.
   * 
   * @param processors
   *          a list of {@link ResourcePreProcessor} processors.
   */
  void setJsPreProcessors(final List<ResourcePreProcessor> processors);

  /**
   * Provide a list of preProcessors to apply on any type of content.
   * 
   * @param processors
   *          a list of {@link ResourcePreProcessor} processors.
   */
  void setAnyResourcePreProcessors(final List<ResourcePreProcessor> processors);

  /**
   * Provide a list of postProcessors to apply on css content.
   * 
   * @param processors
   *          a list of {@link ResourcePostProcessor} processors.
   */
  void setCssPostProcessors(final List<ResourcePostProcessor> processors);

  /**
   * Provide a list of postProcessors to apply on js content.
   * 
   * @param processors
   *          a list of {@link ResourcePostProcessor} processors.
   */
  void setJsPostProcessors(final List<ResourcePostProcessor> processors);

  /**
   * Provide a list of postProcessors to apply on any type of content.
   * 
   * @param processors
   *          a list of {@link ResourcePostProcessor} processors.
   */
  void setAnyResourcePostProcessors(final List<ResourcePostProcessor> processors);

}
