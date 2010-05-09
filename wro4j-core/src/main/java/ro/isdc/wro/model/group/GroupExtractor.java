/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import javax.servlet.http.HttpServletRequest;

import ro.isdc.wro.model.resource.ResourceType;


/**
 * Perform processing of the uri and extracts group & resource related informations: name of the group, resource type
 * and whether the result should produce minimized version.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface GroupExtractor {
  /**
   * Retrieves a set of group names from supplied uri.
   *
   * @param request
   *          to check.
   * @return found group name. If no group is found, null is returned.
   */
  String getGroupName(final HttpServletRequest request);

  /**
   * @param request
   *          to check.
   * @return requested ResourceType.
   */
  ResourceType getResourceType(final HttpServletRequest request);

  /**
   * @param request {@link HttpServletRequest} object.
   * @return true if the expected result must be minimized.
   */
  boolean isMinimized(final HttpServletRequest request);
}
