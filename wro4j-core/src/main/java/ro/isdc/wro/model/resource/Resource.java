/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource;

import org.apache.commons.lang.builder.ToStringBuilder;

import ro.isdc.wro.model.group.Group;


/**
 * Encapsulates information about a resource. This class is not final because we need to mock it in unit tests.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public class Resource {
  /**
   * The type of resource.
   */
  private final ResourceType type;

  /**
   * Resource identifier.
   */
  private final String uri;

  /**
   * The reference to the group where this resource resides.
   */
  private Group group;

  /**
   * Constructor.
   *
   * @param uri
   *          of the resource.
   * @param type
   *          of the resource.
   */
  private Resource(final String uri, final ResourceType type) {
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be null!");
    }
    if (type == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    this.uri = cleanUri(uri);
    this.type = type;
  }


	/**
	 * Factory method for Resource creation. A factory method is preferred instead of public constructor, in order to
	 * avoid possibilities for clients to extend Resource class.
	 *
	 * @return an instance of {@link Resource} object.
	 */
  public static Resource create(final String uri, final ResourceType type) {
  	return new Resource(uri, type);
  }

  /**
   * Perform a cleaning of the uri by trimming it and removing last '/'
   * character if exists.
   *
   * @param uri
   *          to clean.
   * @return cleaned uri.
   */
  private static String cleanUri(final String uri) {
    String result = uri.trim();
    final int endIndex = result.length() - 1;
    if (result.lastIndexOf('/') == endIndex) {
      result = result.substring(0, endIndex);
    }
    return result;
  }

  /**
   * @return the type
   */
  public ResourceType getType() {
    return type;
  }

  /**
   * @return the uri associated with this resource.
   */
  public String getUri() {
    return uri;
  }

  /**
   * Do NOT call this explicitly, it is used only to build groups.
   *
   * @param group the group to set
   */
  public void setGroup(final Group group) {
    this.group = group;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Resource) {
      final Resource resource = (Resource) obj;
      return getUri().equals(resource.getUri()) && getType().equals(resource.getType());
    }
    return false;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("uri", uri).toString();
  }
}
