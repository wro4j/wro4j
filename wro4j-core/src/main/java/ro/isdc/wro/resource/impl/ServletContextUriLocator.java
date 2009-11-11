/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.resource.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.util.WroUtil;

/**
 * UriLocator capable to read the resources relative to servlet context. The
 * resource reader will attempt to locate a physic resource under the servlet
 * context and if the resource does not exist, will try to use
 * requestDispatcher. This kind of resources will be accepted if their prefix is
 * <code>/</code>.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 10, 2008
 */
public final class ServletContextUriLocator implements UriLocator {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(ServletContextUriLocator.class);

  /**
   * Prefix for url resources.
   */
  private static final String PREFIX = "/";

  /**
   * Locator of dynamic resources. There can be different strategies. We will
   * always use only this. Try to switch later to see if performance change.
   */
  private final DynamicStreamLocatorStrategy dynamicStreamLocator = new ByteArrayStreamDispatchingStrategy();

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return uri.startsWith(PREFIX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri) throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be NULL!");
    }
    log.debug("uri resource: " + uri);
    final ServletContext servletContext = Context.get().getServletContext();
    // first attempt
    InputStream inputStream = servletContext.getResourceAsStream(uri);
    if (inputStream == null) {
      final HttpServletRequest request = Context.get().getRequest();
      final HttpServletResponse response = Context.get().getResponse();
      inputStream = dynamicStreamLocator.getInputStream(request, response, uri);
    }
    if (inputStream == null) {
      throw new IOException("Exception while reading resource from " + uri);
    }
    return inputStream;
  }

  /**
   * DynamicStreamLocatorStrategy. Defines the way a inputStream is located
   * using different types of streams after dispatching the request to provided
   * location.
   */
  private interface DynamicStreamLocatorStrategy {
    /**
     * @param request
     *          {@link HttpServletRequest} object.
     * @param response
     *          {@link HttpServletResponse} object.
     * @param location
     *          where to dispatch.
     * @return InputStream of the dispatched resource.
     * @throws IOException
     *           if an input or output exception occurred
     */
    InputStream getInputStream(final HttpServletRequest request,
        final HttpServletResponse response, final String location)
        throws IOException;
  }

  /**
   * A strategy which use PipedStreams with a separate thread for writing and
   * dispatches the request to provided location. The main disadvantage of this
   * strategy is that it creates a separate thread, that is not recommended by
   * servletContainer such as WebLogic.
   */
  private final class PipedStreamDispatchingStrategy implements
      DynamicStreamLocatorStrategy {
    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream(final HttpServletRequest request,
        final HttpServletResponse response, final String location)
        throws IOException {
      log.debug("</getInputStream>");
      // create piped IO streams
      final PipedInputStream pis = new PipedInputStream();
      final PipedOutputStream pos = new PipedOutputStream(pis);
      // Wrap request
      final ServletRequest wrappedRequest = new HttpServletRequestWrapper(
          request) {
        @Override
        public String getPathInfo() {
          return WroUtil.getPathInfoFromLocation(location);
        }

        @Override
        public String getServletPath() {
          return WroUtil.getServletPathFromLocation(location);
        }
      };
      // Wrap response
      final ServletResponse wrappedResponse = new HttpServletResponseWrapper(
          response) {
        /**
         * Both servletOutputStream and PrintWriter must be overriden in order
         * to be sure that dispatched servlet will write to the pipe.
         * PrintWrapper of wrapped response.
         */
        private final PrintWriter pw = new PrintWriter(pos);

        /**
         * Servlet output stream of wrapped response.
         */
        private final ServletOutputStream sos = new DelegatingServletOutputStream(pos);

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
          return sos;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
          return pw;
        }
      };
      // run dispatching in separate thread, in order to avoid deadlock of the
      // main thread when using Piped IO streams.
      new Thread() {
        @Override
        public void run() {
          try {
            request.getRequestDispatcher(location).include(wrappedRequest,
                wrappedResponse);
            // force flushing - the content will be written to
            // PipedOutputStream. Otherwise only 32K of data will be written.
            wrappedResponse.getWriter().flush();
            pos.close();
          } catch (final Exception se) {
            throw new WroRuntimeException(
                "Error while dispatching the request: " + se.getMessage());
          }
        };
      }.start();
      log.debug("</getInputStream>");
      return pis;
    }
  }

  /**
   * A strategy which use ByteArray IO Streams and dispatch the request to a
   * given location.
   */
  private final class ByteArrayStreamDispatchingStrategy implements
      DynamicStreamLocatorStrategy {
    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream(final HttpServletRequest request,
        final HttpServletResponse response, final String location)
        throws IOException {
      // where to write the bytes of the stream
      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      // Wrap request
      final ServletRequest wrappedRequest = new HttpServletRequestWrapper(
          request) {
        @Override
        public String getPathInfo() {
          return WroUtil.getPathInfoFromLocation(location);
        }

        @Override
        public String getServletPath() {
          return WroUtil.getServletPathFromLocation(location);
        }
      };
      // Wrap response
      final ServletResponse wrappedResponse = new HttpServletResponseWrapper(
          response) {
        /**
         * Both servletOutputStream and PrintWriter must be overriden in order
         * to be sure that dispatched servlet will write to the pipe.
         * PrintWrapper of wrapped response.
         */
        private final PrintWriter pw = new PrintWriter(os);

        /**
         * Servlet output stream of wrapped response.
         */
        private final ServletOutputStream sos = new DelegatingServletOutputStream(os);

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
          return sos;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
          return pw;
        }
      };
      // use dispatcher
      try {
        request.getRequestDispatcher(location).include(wrappedRequest,
            wrappedResponse);
        // force flushing - the content will be written to
        // BytArrayOutputStream. Otherwise exactly 32K of data will be
        // written.
        wrappedResponse.getWriter().flush();
        os.close();
      } catch (final ServletException e) {
        throw new WroRuntimeException("Error while dispatching the request: "
            + e.getMessage());
      }
      return new ByteArrayInputStream(os.toByteArray());
    }
  }
}
