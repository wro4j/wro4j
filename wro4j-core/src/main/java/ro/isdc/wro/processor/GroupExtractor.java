/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.util.List;

import ro.isdc.wro.resource.ResourceType;


/**
 * Perform processing of the uri and extracts group & resources related informations: a list of groups, resource type
 * and name.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface GroupExtractor {
  /**
   * Retrieves a list of group names from supplied uri.
   *
   * @param uri
   *          to check.
   * @return a list of found group names.
   */
  List<String> getGroupNames(final String uri);

  /**
   * @param uri
   *          to check.
   * @return requested ResourceType from uri.
   */
  ResourceType getResourceType(final String uri);
}
