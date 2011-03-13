/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource;

import org.apache.commons.lang.builder.ToStringBuilder;


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
  private ResourceType type;
  /**
   * Resource identifier.
   */
  private String uri;
  /**
   * Used to skip minimization during pre processing. Useful when you know that the resource is already minimized and no
   * minimization is needed. Default value is true.
   */
  private boolean minimize = true;

  /**
   * Empty constructor.
   */
  public Resource() {
  }

  /**
   * Constructor.
   *
   * @param uri of the resource.
   * @param type of the resource.
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
   * Perform a cleaning of the uri by trimming it and removing last '/' character if exists.
   *
   * @param uri to clean.
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
   * @param type the type to set
   */
  public void setType(final ResourceType type) {
    if (type == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    this.type = type;
  }


  /**
   * @param uri the uri to set
   */
  public void setUri(final String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("Uri cannot be null!");
    }
    this.uri = cleanUri(uri);
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
   * @return the minimize
   */
  public boolean isMinimize() {
    return this.minimize;
  }


  /**
   * @param minimize the minimize to set
   */
  public void setMinimize(final boolean minimize) {
    this.minimize = minimize;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Resource) {
      final Resource resource = (Resource)obj;
      return getUri().equals(resource.getUri()) && getType().equals(resource.getType());
    }
    return false;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + getType().hashCode();
    hash = 31 * hash + getUri().hashCode();
    return hash;
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("uri", uri).append("minimize", minimize).toString();
  }
}
