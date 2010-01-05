/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.processor.impl;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.processor.GroupsExtractor;
import ro.isdc.wro.resource.ResourceType;

/**
 * Default implementation which is capable of extracting a single group from one request uri.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public final class SingleGroupExtractor implements GroupsExtractor {
  /**
   * {@inheritDoc}
   */
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
      throw new WroRuntimeException("Invalid group name in the uri: '" + uri + "'");
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
      // invalid exception
      // TODO Auto-generated method stub
      throw new WroRuntimeException("Invalid uri: '" + uri + "'");
    }
    return type;
  }

}
