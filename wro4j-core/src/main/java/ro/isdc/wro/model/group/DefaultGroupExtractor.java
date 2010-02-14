/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.model.group;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;

/**
 * Default implementation which is capable of extracting a single group from one request uri.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public final class DefaultGroupExtractor implements GroupExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultGroupExtractor.class);
  /**
   * {@inheritDoc}
   */
  public String getGroupName(final String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("Uri cannot be null!");
    }
    final String groupName = FilenameUtils.getBaseName(uri);
    return StringUtils.isEmpty(groupName) ? null : groupName;
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
    final String extension = FilenameUtils.getExtension(uri);
    ResourceType type = null;
    try {
      type = ResourceType.valueOf(extension.toUpperCase());
    } catch (final IllegalArgumentException e) {
      LOG.debug("Cannot identify resourceType for uri: " + uri);
    }
    return type;
  }

}
