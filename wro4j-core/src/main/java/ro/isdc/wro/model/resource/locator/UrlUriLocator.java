/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;


/**
 * UriLocator capable to read the resources from some URL. Usually, this uriLocator will be the last in the chain of
 * uriLocators.
 *
 * @author Alex Objelean
 * @created Created on Nov 10, 2008
 */
public class UrlUriLocator extends WildcardUriLocatorSupport {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UrlUriLocator.class);


  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return isValid(uri);
  }


  /**
   * Check if a uri is a URL resource.
   *
   * @param uri to check.
   * @return true if the uri is a URL resource.
   */
  public static boolean isValid(final String uri) {
    // if creation of URL object doesn't throw an exception, the uri can be
    // accepted.
    try {
      new URL(uri);
    } catch (final MalformedURLException e) {
      return false;
    }
    return true;
  }


  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
    throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("uri cannot be NULL!");
    }
    LOG.debug("Reading uri: " + uri);
    if (getWildcardStreamLocator().hasWildcard(uri)) {
      final String fullPath = FilenameUtils.getFullPath(uri);
      final URL url = new URL(fullPath);
      return getWildcardStreamLocator().locateStream(uri, new File(url.getFile()));
    }
    final URL url = new URL(uri);
    final URLConnection con = url.openConnection();
    // sets the "UseCaches" flag to <code>false</code>, mainly to avoid jar file locking on Windows.
    con.setUseCaches(false);
    return new BufferedInputStream(con.getInputStream());
  }
}
