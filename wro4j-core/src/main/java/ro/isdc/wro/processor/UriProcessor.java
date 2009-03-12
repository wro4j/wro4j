/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.util.List;

import ro.isdc.wro.resource.ResourceType;

/**
 * UriProcessor.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public interface UriProcessor {
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
