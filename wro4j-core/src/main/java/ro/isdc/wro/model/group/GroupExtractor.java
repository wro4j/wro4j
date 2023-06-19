/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import jakarta.servlet.http.HttpServletRequest;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * Perform processing of the uri and extracts group and resource related informations: name of the group, resource type
 * and whether the result should produce minimized version.
 *
 * @author Alex Objelean
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


  /**
   * This method is a opposite of the other 3 methods. Instead of decoding, it encodes groupName, resourceType and
   * minimize option into url. It should not return entire url, but only the last part.
   *
   * @return a part of the url path which encodes the groupName, resourceType and minimize option.
   */
  String encodeGroupUrl(final String groupName, final ResourceType resourceType, final boolean minimize);
}
