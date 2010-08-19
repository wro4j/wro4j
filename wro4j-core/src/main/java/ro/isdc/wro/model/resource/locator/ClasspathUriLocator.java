/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.util.StringUtils;


/**
 * Implementation of the {@link UriLocator} that is able to read a resource from a classpath.
 *
 * @author Alex Objelean
 * @created Created on Nov 6, 2008
 */
public class ClasspathUriLocator
    extends WildcardUriLocatorSupport {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathUriLocator.class);

  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = "classpath:";

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String url) {
    return isValid(url);
  }

  /**
   * Check if a uri is a classpath resource.
   *
   * @param uri
   *          to check.
   * @return true if the uri is a classpath resource.
   */
  public static boolean isValid(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be NULL!");
    }
    LOG.debug("Reading uri: " + uri);
    // replace prefix & clean path by removing '..' characters if exists and
    // normalizing the location to use.
    final String location = StringUtils.cleanPath(uri.replaceFirst(PREFIX, ""));

    if (getWildcardStreamLocator().hasWildcard(location)) {
      LOG.debug("wildcard detected for location: " + location);
      final String fullPath = FilenameUtils.getFullPath(location);
      //final URL url = getClass().getClassLoader().getSystemResource(fullPath);
      final URL url = ClassLoader.getSystemResource(fullPath);
      if (url == null) {
        final String message = "Couldn't get URL for the following path: " + fullPath;
        LOG.warn(message);
        throw new IOException(message);
      }
      return getWildcardStreamLocator().locateStream(location, new File(url.getFile()));
    }

    try {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      // TODO check if this is needed
      final InputStream is = classLoader.getResourceAsStream(location);
      if (is == null) {
        throw new NullPointerException();
      }
      final URL url = classLoader.getResource(location);
      return url.openStream();
    } catch (final NullPointerException e) {
      throw new IOException("Couldn't get InputStream from this resource: " + uri);
    }
  }
}
