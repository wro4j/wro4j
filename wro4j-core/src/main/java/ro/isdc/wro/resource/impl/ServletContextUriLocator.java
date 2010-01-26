/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.resource.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * UriLocator capable to read the resources relative to servlet context. The resource reader will attempt to locate a
 * physic resource under the servlet context and if the resource does not exist, will try to use requestDispatcher. This
 * kind of resources will be accepted if their prefix is <code>/</code>.
 *
 * @author Alex Objelean
 * @created Created on Nov 10, 2008
 */
public final class ServletContextUriLocator
  implements UriLocator {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextUriLocator.class);

  /**
   * Prefix for url resources.
   */
  public static final String PREFIX = "/";

  /**
   * Locator of dynamic resources. There can be different strategies. We will always use only this. Try to switch later
   * to see if performance change.
   */
  private final DynamicStreamLocatorStrategy dynamicStreamLocator = new ByteArrayStreamDispatchingStrategy();// new
                                                                                                             // PipedStreamDispatchingStrategy();


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
    return uri.trim().startsWith(ServletContextUriLocator.PREFIX);
  }


  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
    throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be NULL!");
    }
    LOG.debug("uri resource: " + uri);
    final ServletContext servletContext = Context.get().getServletContext();
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

  /**
   * DynamicStreamLocatorStrategy. Defines the way a inputStream is located using different types of streams after
   * dispatching the request to provided location.
   */
  private interface DynamicStreamLocatorStrategy {
    /**
     * @param request {@link HttpServletRequest} object.
     * @param response {@link HttpServletResponse} object.
     * @param location where to dispatch.
     * @return InputStream of the dispatched resource.
     * @throws IOException if an input or output exception occurred
     */
    InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
      final String location)
      throws IOException;
  }

  /**
   * A strategy which use ByteArray IO Streams and dispatch the request to a given location.
   */
  private static final class ByteArrayStreamDispatchingStrategy
    implements DynamicStreamLocatorStrategy {
    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
      final String location)
      throws IOException {
      // where to write the bytes of the stream
      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      // Wrap request
      final HttpServletRequest wrappedRequest = getWrappedServletRequest(request, location);
      // Wrap response
      final ServletResponse wrappedResponse = getWrappedServletResponse(response, os);
      // use dispatcher
      try {
        request.getRequestDispatcher(location).include(wrappedRequest, wrappedResponse);
        LOG.debug("dispatching request to:" + location);
        // force flushing - the content will be written to
        // BytArrayOutputStream. Otherwise exactly 32K of data will be
        // written.
        wrappedResponse.getWriter().flush();
        os.close();
      } catch (final ServletException e) {
        throw new WroRuntimeException("Error while dispatching the request for location " + location, e);
      }
      if (os.size() == 0) {
        LOG.warn("Wrong or empty resource with location : " + location);
      }
      return new ByteArrayInputStream(os.toByteArray());
    }


    /**
     * Build a wrapped servlet request which will be used for dispatching.
     */
    private HttpServletRequest getWrappedServletRequest(final HttpServletRequest request, final String location) {
      final HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
        @Override
        public String getRequestURI() {
          return getContextPath() + location;
        }


        @Override
        public String getPathInfo() {
          return WroUtil.getPathInfoFromLocation(location);
        }


        @Override
        public String getServletPath() {
          return WroUtil.getServletPathFromLocation(location);
        }
      };
      return wrappedRequest;
    }


    /**
     * Build a wrapped servlet response which will be used for dispatching.
     */
    private ServletResponse getWrappedServletResponse(final HttpServletResponse response, final ByteArrayOutputStream os) {
      /**
       * Both servletOutputStream and PrintWriter must be overriden in order to be sure that dispatched servlet will
       * write to the pipe.
       */
      final ServletResponse wrappedResponse = new HttpServletResponseWrapper(response) {
        /**
         * PrintWrapper of wrapped response.
         */
        private final PrintWriter pw = new PrintWriter(os);

        /**
         * Servlet output stream of wrapped response.
         */
        private final ServletOutputStream sos = new DelegatingServletOutputStream(os);

        @Override
        public ServletOutputStream getOutputStream()
          throws IOException {
          return sos;
        }


        @Override
        public PrintWriter getWriter()
          throws IOException {
          return pw;
        }
      };
      return wrappedResponse;
    }
  }
}
