/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import ro.isdc.wro.model.resource.ResourceType;


/**
 * Perform processing of the uri and extracts group & resources related informations: a list of groups, resource type
 * and name.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface GroupExtractor {
  /**
   * Retrieves a set of group names from supplied uri.
   *
   * @param uri
   *          to check.
   * @return found group name. If no group is found, null is returned.
   */
  String getGroupName(final String uri);

  /**
   * @param uri
   *          to check.
   * @return requested ResourceType from uri if exists or null otherwise.
   */
  ResourceType getResourceType(final String uri);
}
