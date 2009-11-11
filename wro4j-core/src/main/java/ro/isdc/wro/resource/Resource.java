/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;

/**
 * Encapsulates information about a resource. Resource instance is an immutable
 * thread-safe object.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public final class Resource {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(Resource.class);

  /**
   * The type of resource.
   */
  private final ResourceType type;

  /**
   * Resource identifier.
   */
  private final String uri;

  /**
   * UriLocator.
   */
  private final UriLocator uriLocator;

  /**
   * Constructor.
   *
   * @param uri
   *          of the resource.
   * @param type
   *          of the resource.
   * @param uriLocator
   *          {@link UriLocator} object.
   */
  public Resource(final String uri, final ResourceType type,
      final UriLocator uriLocator) {
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be null!");
    }
    if (type == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    if (uriLocator == null) {
      throw new IllegalArgumentException("UriLocator cannot be null!");
    }
    this.uri = cleanUri(uri);
    this.type = type;
    this.uriLocator = uriLocator;
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
   * TODO cache reader instance?
   *
   * @return the reader of this resource. The reader cannot be NULL.
   * @throws IOException
   *           if the resource is unavailable.
   * @throws WroRuntimeException
   *           if Reader is null.
   */
  public Reader getReader() throws IOException {
    // TODO add charset to constructor to avoid encoding issues
    log.debug("getReader for " + this.getUri());
    final InputStream is = uriLocator.locate(this.getUri());
    // wrap reader with bufferedReader for top efficiency
    final Reader reader = new BufferedReader(new InputStreamReader(is));
    if (reader == null) {
      throw new WroRuntimeException(
          "Exception while retrieving InputStream from uri: " + getUri());
    }
    return reader;
  }

  /**
   * @return the type
   */
  public final ResourceType getType() {
    return type;
  }

  /**
   * @return the uri
   */
  public final String getUri() {
    return uri;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append(
        "uriLocator", this.uriLocator.getClass()).append("type", this.type)
        .append("uri", this.uri).toString();
  }
}
