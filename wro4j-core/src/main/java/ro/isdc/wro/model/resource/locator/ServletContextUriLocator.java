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
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer.NoMoreAttemptsIOException;
import ro.isdc.wro.util.WroUtil;


/**
 * UriLocator capable to read the resources relative to servlet context. The resource reader will attempt to locate a
 * physic resource under the servlet context and if the resource does not exist, will try to use requestDispatcher. This
 * kind of resources will be accepted if their prefix is '/'.
 *
 * @author Alex Objelean, Ivar Conradi Østhus
 * @created Created on Nov 10, 2008, Updated on March 2, 2012
 *
 */
public class ServletContextUriLocator
  extends WildcardUriLocatorSupport {
  /**
   * Logger for this class.
   */
  static final Logger LOG = LoggerFactory.getLogger(ServletContextUriLocator.class);

  /**
   * Prefix for url resources.
   */
  public static final String PREFIX = "/";
  /**
   * Constant for WEB-INF folder.
   */
  private static final String PROTECTED_PREFIX = "/WEB-INF/";
  /**
   * Locates a stream using request dispatcher.
   */
  private final DispatcherStreamLocator dispatcherStreamLocator = new DispatcherStreamLocator();
  /**
   * Determines the order of dispatcher resource locator and servlet context based resource locator.
   */
  private LocatorStrategy locatorStrategy = LocatorStrategy.DISPATCHER_FIRST;

  /**
   * Available LocatorStrategies.
   *
   * DISPATCHER_FIRST is default option. This means this UriLocator will first try to locate resource via the
   * dispatcher stream locator. This will include dynamic resources produces by servlet's or JSP's. If the specified
   * resource cannot be found with the dispatcherStreamLocator the implementation will try to use the ServletContext to
   * locate the resource.
   *
   * SERVLET_CONTEXT_FIRST is a alternative approach where we will first try to locate the resource VIA the
   * ServletContext first, and then use the dispatcheStreamLocator if not found. In some cases, where you do not rely
   * on dynamic resources this can be a more reliable and a more efficient approach.
   *
   *
   */
  public static enum LocatorStrategy {
    DISPATCHER_FIRST,
    SERVLET_CONTEXT_FIRST
  }

  public ServletContextUriLocator() {
  }

  /**
   * Sets the locator strategy to use.
   * @param locatorStrategy
   */
  public UriLocator setLocatorStrategy(LocatorStrategy locatorStrategy) {
    this.locatorStrategy = locatorStrategy;
    return this;
  }

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
    Validate.notNull(uri, "URI cannot be NULL!");
    LOG.debug("locate resource: {}", uri);
    
    try {
      if (getWildcardStreamLocator().hasWildcard(uri)) {
        final ServletContext servletContext = Context.get().getServletContext();
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
      /**
       * This is a special case when no more attempts are required, since the required computation was achieved
       * successfully. This solves the following <a
       * href="http://code.google.com/p/wro4j/issues/detail?id=321">issue</a>.<p/>
       * The problem was that in some situations,
       * when the dispatcherStreamLocator was used to locate resources containing wildcard, the following message was
       * printed to the console: <code>SEVERE: Servlet.service() for servlet default threw exception
       * java.io.FileNotFoundException.</code>
       */
      if (e instanceof NoMoreAttemptsIOException) {
        throw e;
      }
      LOG.warn("Couldn't localize the stream containing wildcard. Original error message: '{}'", e.getMessage()
        + "\".\n Trying to locate the stream without the wildcard.");
    }

    InputStream inputStream = null;
    if (locatorStrategy.equals(LocatorStrategy.DISPATCHER_FIRST)) {
        inputStream = dispatcherFirstStreamLocator(uri);
    } else {
        inputStream = servletContextFirstStreamLocator(uri);
    }
      
    validateInputStreamIsNotNull(inputStream, uri);
    
    return inputStream;
  }

    private InputStream servletContextFirstStreamLocator(String uri)
      throws IOException {
      try {
          return servletContextBasedStreamLocator(uri);
      } catch (final IOException e) {
          LOG.debug("retrieving servletContext stream for uri: {}", uri);
          return dispatcherBasedStreamLocator(uri);
      }
    }

    private InputStream dispatcherFirstStreamLocator(String uri)
      throws IOException {
      try {
          return dispatcherBasedStreamLocator(uri);
      } catch (final IOException e) {
          LOG.debug("retrieving servletContext stream for uri: {}", uri);
          return servletContextBasedStreamLocator(uri);
      }
    }

    private InputStream dispatcherBasedStreamLocator(String uri)
      throws IOException {
      final HttpServletRequest request = Context.get().getRequest();
      final HttpServletResponse response = Context.get().getResponse();
      // The order of stream retrieval is important. We are trying to get the dispatcherStreamLocator in order to handle
      // jsp resources (if such exist). Switching the order would cause jsp to not be interpreted by the container.
      return dispatcherStreamLocator.getInputStream(request, response, uri);
    }

    private InputStream servletContextBasedStreamLocator(String uri)
      throws IOException {
      final ServletContext servletContext = Context.get().getServletContext();
      return servletContext.getResourceAsStream(uri);
    }

    private void validateInputStreamIsNotNull(InputStream inputStream, String uri)
      throws IOException {
      if (inputStream == null) {
        LOG.error("Exception while reading resource from " + uri);
        throw new IOException("Exception while reading resource from " + uri);
      }
    }
}
