/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.processor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.processor.RequestUriParser;
import ro.isdc.wro.resource.ResourceType;

/**
 * Default implementation which is capable of extracting a single group from one request uri.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public final class SingleGroupRequestUriParser implements RequestUriParser {
  private static final Logger LOG = LoggerFactory.getLogger(SingleGroupRequestUriParser.class);
  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<String> getGroupNames(final String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("Uri cannot be null!");
    }
    final List<String> groupNames = new ArrayList<String>();
    try {
      // find last dot & get extension (js & css)
      final int lastDot = uri.lastIndexOf('.');
      final String beforeDot = uri.substring(0, lastDot);
      final int lastSlash = beforeDot.lastIndexOf('/');
      final String groupName = beforeDot.substring(lastSlash + 1, beforeDot
          .length());
      groupNames.add(groupName);
    } catch (final IndexOutOfBoundsException e) {
      LOG.warn("No group defined in uri: '" + uri + "'");
      return Collections.EMPTY_LIST;
    }
    return groupNames;
  }

  /**
   * Extracts the resource type, by parsing the uri & finds the extension. If
   * extension is valid ('css' or 'js'), returns corresponding ResourceType,
   * otherwise throws exception.
   * <p>
   * Valid examples of uri are: <code>/context/somePath/test.js</code> or
   * <code>/context/somePath/test.css</code> {@inheritDoc}
   */
  public ResourceType getResourceType(final String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("Uri cannot be null!");
    }
    // find last dot & get extension (js & css)
    final int lastDot = uri.lastIndexOf('.');
    final String extension = uri.substring(lastDot + 1, uri.length());
    ResourceType type = null;
    try {
      type = ResourceType.valueOf(extension.toUpperCase());
    } catch (final IllegalArgumentException e) {
      LOG.debug("Cannot identify resourceType for uri: " + uri);
    }
    return type;
  }

}
