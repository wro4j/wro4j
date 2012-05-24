/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

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

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.support.RedirectedStreamServletResponseWrapper;
import ro.isdc.wro.util.WroUtil;


/**
 * A strategy which use ByteArray IO Streams and dispatch the request to a given location.
 *
 * @author Alex Objelean
 */
public final class DispatcherStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(DispatcherStreamLocator.class);
  /**
   * Used to locate external resources. No wildcard handling is required.
   */
  private UriLocator externalResourceLocator = new UrlUriLocator() {
    protected boolean disableWildcards() {
      return true;
    };
  };
  
  /**
   * When using JBoss Portal and it has some funny quirks...actually a portal application have several small web
   * application behind it. So when it intercepts a requests for portal then it start bombing the the application behind
   * the portal with multiple threads (web requests) that are combined with threads for wro4j.
   *
   *
   * @return a valid stream for required location. This method will never return a null.
   * @throws IOException if the stream cannot be located at the specified location.
   */
  public InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
    final String location)
    throws IOException {
    Validate.notNull(request);
    Validate.notNull(response);
    // where to write the bytes of the stream
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    boolean warnOnEmptyStream = false;

    // preserve context, in case it is unset during dispatching
    final Context originalContext = Context.get();
    try {
      final RequestDispatcher dispatcher = request.getRequestDispatcher(location);
      if (dispatcher == null) {
        // happens when dynamic servlet context relative resources are included outside of the request cycle (inside
        // the thread responsible for refreshing resources)
        // Returns the part URL from the protocol name up to the query string and contextPath.
        final String servletContextPath = request.getRequestURL().toString().replace(request.getServletPath(), "");
        final String absolutePath = servletContextPath + location;
        return externalResourceLocator.locate(absolutePath);
      }
      // Wrap request
      final ServletRequest servletRequest = getWrappedServletRequest(request, location);
      // Wrap response
      final ServletResponse servletResponse = new RedirectedStreamServletResponseWrapper(os, response);
      LOG.debug("dispatching request to location: " + location);
      // use dispatcher
      dispatcher.include(servletRequest, servletResponse);
      warnOnEmptyStream = true;
      // force flushing - the content will be written to
      // BytArrayOutputStream. Otherwise exactly 32K of data will be
      // written.
      servletResponse.getWriter().flush();
      os.close();
    } catch (final Exception e) {
      // Not only servletException can be thrown, also dispatch.include can throw NPE when the scheduler runs outside
      // of the request cycle, thus connection is unavailable. This is caused mostly when invalid resources are
      // included.
      LOG.debug("[FAIL] Error while dispatching the request for location {}", location);
      throw new IOException("Error while dispatching the request for location " + location);
    } finally {
      if (warnOnEmptyStream && os.size() == 0) {
        LOG.warn("Wrong or empty resource with location: {}", location);
      }
      // Put the context back
      if (!Context.isContextSet()) {
        Context.set(originalContext);
      }
    }
    return new ByteArrayInputStream(os.toByteArray());
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
    return wrappedRequest;
  }
}