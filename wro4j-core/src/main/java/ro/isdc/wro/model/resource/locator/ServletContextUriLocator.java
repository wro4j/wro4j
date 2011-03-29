/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.support.ByteArrayStreamDispatchingStrategy;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.util.WroUtil;


/**
 * UriLocator capable to read the resources relative to servlet context. The resource reader will attempt to locate a
 * physic resource under the servlet context and if the resource does not exist, will try to use requestDispatcher. This
 * kind of resources will be accepted if their prefix is '/'.
 *
 * @author Alex Objelean
 * @created Created on Nov 10, 2008
 */
public class ServletContextUriLocator
  extends WildcardUriLocatorSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextUriLocator.class);
  /**
   * Prefix for url resources.
   */
  public static final String PREFIX = "/";
  /**
   * Constant for WEB-INF folder.
   */
  private static final String PROTECTED_PREFIX = "/WEB-INF/";
  /**
   * Locator of dynamic resources. There can be different strategies. We will always use only this. Try to switch later
   * to see if performance change.
   */
  private final ByteArrayStreamDispatchingStrategy dynamicStreamLocator = new ByteArrayStreamDispatchingStrategy();

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return isValid(uri);
  }


  /**
   * Check if a uri is a servletContext resource.
   *
   * @param uri to check.
   * @return true if the uri is a servletContext resource.
   */
  public static boolean isValid(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }

  /**
   * Check If the uri of the resource is protected: it cannot be accessed by accessing the url directly (WEB-INF
   * folder).
   *
   * @param uri the uri to check.
   * @return true if the uri is a protected resource.
   */
  public static boolean isProtectedResource(final String uri) {
    return WroUtil.startsWithIgnoreCase(uri, PROTECTED_PREFIX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
    throws IOException {
    LOG.debug("locating uri: " + uri);
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be NULL!");
    }
    LOG.debug("uri resource: " + uri);
    final ServletContext servletContext = Context.get().getServletContext();

    try {
      if (getWildcardStreamLocator().hasWildcard(uri)) {
        final String fullPath = FilenameUtils.getFullPath(uri);
        final String realPath = servletContext.getRealPath(fullPath);
        if (realPath == null) {
          final String message = "Could not determine realPath for resource: " + uri;
          LOG.error(message);
          throw new IOException(message);
        }
        return getWildcardStreamLocator().locateStream(uri, new File(realPath));
      }
    } catch (final IOException e) {
      LOG.warn("Couldn't localize the stream containing wildcard. Original error message: \"" + e.getMessage()
        + "\".\n Trying to locate the stream without the wildcard.");
    }

    // first attempt
    InputStream inputStream = servletContext.getResourceAsStream(uri);
    if (inputStream == null) {
      final HttpServletRequest request = Context.get().getRequest();
      final HttpServletResponse response = Context.get().getResponse();
      inputStream = dynamicStreamLocator.getInputStream(request, response, uri);
    }
    if (inputStream == null) {
      LOG.error("Exception while reading resource from " + uri);
      throw new IOException("Exception while reading resource from " + uri);
    }
    return inputStream;
  }
}
