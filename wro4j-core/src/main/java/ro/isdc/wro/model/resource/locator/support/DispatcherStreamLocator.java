/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.support.RedirectedStreamServletResponseWrapper;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * Responsible to locate a context relative resource. Attempts to locate the resource using {@link RequestDispatcher} .
 * If the dispatcher fails, it will fallback resource retrieval to a http call using {@link UrlUriLocator}.
 *
 * @author Alex Objelean
 */
public class DispatcherStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(DispatcherStreamLocator.class);
  /**
   * Attribute indicating that the request is included from within a wro request cycle. This is required to prevent
   * {@link StackOverflowError}.
   *
   * @VisibleForTesting
   */
  public static final String ATTRIBUTE_INCLUDED_BY_DISPATCHER = DispatcherStreamLocator.class.getName()
      + ".included_with_dispatcher";

  /**
   * When using JBoss Portal and it has some funny quirks...actually a portal application have several small web
   * application behind it. So when it intercepts a requests for portal then it start bombing the the application behind
   * the portal with multiple threads (web requests) that are combined with threads for wro4j.
   *
   * @return a valid stream for required location. This method will never return a null.
   * @throws IOException
   *           if the stream cannot be located at the specified location.
   */
  public InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
      final String location)
      throws IOException {
    Validate.notNull(request);
    Validate.notNull(response);

    // where to write the bytes of the stream
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    boolean warnOnEmptyStream = false;

    try {
      final RequestDispatcher dispatcher = request.getRequestDispatcher(location);
      if (dispatcher != null) {
        // Wrap request
        final ServletRequest servletRequest = getWrappedServletRequest(request, location);
        // Wrap response
        final ServletResponse servletResponse = new RedirectedStreamServletResponseWrapper(os, response);
        LOG.debug("dispatching request to location: {}", location);
        // use dispatcher
        dispatcher.include(servletRequest, servletResponse);
        warnOnEmptyStream = true;
        // force flushing - the content will be written to
        // BytArrayOutputStream. Otherwise exactly 32K of data will be
        // written.
        servletResponse.getWriter().flush();
        os.close();
      }
    } catch (final Exception e) {
      LOG.debug("[FAIL] Error while dispatching the request for location {}", location);
      // Not only servletException can be thrown, also dispatch.include can throw NPE when the scheduler runs outside
      // of the request cycle, thus connection is unavailable. This is caused mostly when invalid resources are
      // included.
      return locateExternal(request, location);
    }
    try {
      // fallback to external resource locator if the dispatcher is empty
      if (os.size() == 0) {
        return locateExternal(request, location);
      }
    } finally {
      if (warnOnEmptyStream && os.size() == 0) {
        LOG.debug("Wrong or empty resource with location: {}", location);
      }
    }
    return new ByteArrayInputStream(os.toByteArray());
  }

  private InputStream locateExternal(final HttpServletRequest request, final String location)
      throws IOException {
    // Returns the part URL from the protocol name up to the query string and contextPath.
    final String servletContextPath = request.getRequestURL().toString().replace(request.getServletPath(), "");
    final String absolutePath = servletContextPath + location;
    return createExternalResourceLocator().locate(absolutePath);
  }

  /**
   * @return the {@link UriLocator} responsible for locating resources when dispatcher fails. No wildcard handling is
   *         required.
   * @VisibleForTesting
   */
  UriLocator createExternalResourceLocator() {
    final UriLocator locator = new UrlUriLocator() {
      @Override
      public boolean isEnableWildcards() {
        return false;
      };
    };
    return locator;
  }

  /**
   * Build a wrapped servlet request which will be used for dispatching.
   */
  private ServletRequest getWrappedServletRequest(final HttpServletRequest request, final String location) {
    final HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
      @Override
      public String getRequestURI() {
        return getContextPath() + location;
      }

      @Override
      public String getPathInfo() {
        return WroUtil.getPathInfoFromLocation(this, location);
      }

      @Override
      public String getServletPath() {
        return WroUtil.getServletPathFromLocation(this, location);
      }
    };
    // add an attribute to mark this request as included from wro
    wrappedRequest.setAttribute(ATTRIBUTE_INCLUDED_BY_DISPATCHER, Boolean.TRUE);
    return wrappedRequest;
  }

  /**
   * @param request
   * @return true if the request is included from within wro request cycle.
   */
  public static boolean isIncludedRequest(final HttpServletRequest request) {
    Validate.notNull(request);
    return request.getAttribute(ATTRIBUTE_INCLUDED_BY_DISPATCHER) != null;
  }
}