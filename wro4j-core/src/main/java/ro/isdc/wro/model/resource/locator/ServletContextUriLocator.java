/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.util.WroUtil;


/**
 * UriLocator capable to read the resources relative to servlet context. The resource reader will attempt to locate a
 * physic resource under the servlet context and if the resource does not exist, will try to use requestDispatcher. This
 * kind of resources will be accepted if their prefix is <code>/</code>.
 *
 * @author Alex Objelean
 * @created Created on Nov 10, 2008
 */
public class ServletContextUriLocator
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
   * Constant for WEB-INF folder.
   */
  private static final String PROTECTED_PREFIX = "/WEB-INF/";
  /**
   * Locator of dynamic resources. There can be different strategies. We will always use only this. Try to switch later
   * to see if performance change.
   */
  private final DynamicStreamLocatorStrategy dynamicStreamLocator = new ByteArrayStreamDispatchingStrategy();


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
  private static interface DynamicStreamLocatorStrategy {
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
     * When using JBoss Portal and it has some funny quirks...actually a portal application have several small web
     * application behind it. So when it intercepts a requests for portal then it start bombing the the application
     * behind the portal with multiple threads (web requests) that are combined with threads for wro4.
     */
    public synchronized InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
      final String location)
      throws IOException {
      // where to write the bytes of the stream
      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        final RequestDispatcher dispatcher = request.getRequestDispatcher(location);
        if (dispatcher == null) {
          // happens when dynamic servlet context relative resources are included outside of the request cycle (inside
          // the thread responsible for refreshing resources)

          //Returns the part URL from the protocol name up to the query string and contextPath.
          final String servletContextPath = request.getRequestURL().toString().replace(request.getServletPath(), "");

          final String absolutePath = servletContextPath + location;
          final URL url = new URL(absolutePath);
          return url.openStream();
        }
        // Wrap request
        final HttpServletRequest wrappedRequest = getWrappedServletRequest(request, location);
        // Wrap response
        final ServletResponse wrappedResponse = getWrappedServletResponse(response, os);
        // use dispatcher
        dispatcher.include(wrappedRequest, wrappedResponse);
        LOG.debug("dispatching request to:" + location);
        // force flushing - the content will be written to
        // BytArrayOutputStream. Otherwise exactly 32K of data will be
        // written.
        wrappedResponse.getWriter().flush();
        os.close();
      } catch (final Exception e) {
        // Not only servletException can be thrown, also dispatch.include can throw NPE when the scheduler runs outside
        // of the request cycle, thus connection is unavailable. This is caused mostly when invalid resources are
        // included.
        throw new IOException("Error while dispatching the request for location " + location);
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
