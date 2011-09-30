/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.util.WroUtil;


/**
 * A strategy which use ByteArray IO Streams and dispatch the request to a given location.
 *
 * @author Alex Objelean
 */
public final class DispatcherStreamLocator {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DispatcherStreamLocator.class);

  /**
   * When using JBoss Portal and it has some funny quirks...actually a portal application have several small web
   * application behind it. So when it intercepts a requests for portal then it start bombing the the application behind
   * the portal with multiple threads (web requests) that are combined with threads for wro4j.
   *
   *
   * @return a valid stream for required location. This method will never return a null.
   * @throws IOException
   *           if the stream cannot be located at the specified location.
   */
  public synchronized InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
      final String location)
      throws IOException {
    Validate.notNull(request);
    Validate.notNull(response);
    // where to write the bytes of the stream
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    boolean warnOnEmptyStream = false;

    //preserve context, in case it is unset during dispatching
    final Context originalContext = Context.get();
    try {
      final RequestDispatcher dispatcher = request.getRequestDispatcher(location);
      if (dispatcher == null) {
        // happens when dynamic servlet context relative resources are included outside of the request cycle (inside
        // the thread responsible for refreshing resources)

        // Returns the part URL from the protocol name up to the query string and contextPath.
        final String servletContextPath = request.getRequestURL().toString().replace(request.getServletPath(), "");

        final String absolutePath = servletContextPath + location;
        final URL url = new URL(absolutePath);
        return url.openStream();
      }
      // Wrap request
      final ServletRequest wrappedRequest = getWrappedServletRequest(request, location);
      // Wrap response
      final ServletResponse wrappedResponse = getWrappedServletResponse(response, os);
      LOG.debug("dispatching request to location: " + location);
      // use dispatcher
      dispatcher.include(wrappedRequest, wrappedResponse);
      warnOnEmptyStream = true;
      // force flushing - the content will be written to
      // BytArrayOutputStream. Otherwise exactly 32K of data will be
      // written.
      wrappedResponse.getWriter().flush();
      os.close();
    } catch (final Exception e) {
      // Not only servletException can be thrown, also dispatch.include can throw NPE when the scheduler runs outside
      // of the request cycle, thus connection is unavailable. This is caused mostly when invalid resources are
      // included.
      LOG.debug("[FAIL] Error while dispatching the request for location {}", location, e);
      throw new IOException("Error while dispatching the request for location " + location, e);
    } finally {
      if (warnOnEmptyStream && os.size() == 0) {
        LOG.warn("Wrong or empty resource with location: {}", location);
      }
      //Put the context back
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
     * Both servletOutputStream and PrintWriter must be overriden in order to be sure that dispatched servlet will write
     * to the pipe.
     */
    final HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response) {
      /**
       * PrintWrapper of wrapped response.
       */
      private PrintWriter pw = new PrintWriter(os);

      /**
       * Servlet output stream of wrapped response.
       */
      private ServletOutputStream sos = new DelegatingServletOutputStream(os);

      /**
       * {@inheritDoc}
       */
      @Override
      public void sendError(final int sc)
        throws IOException {
        onError(sc, "");
        super.sendError(sc);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void sendError(final int sc, final String msg)
        throws IOException {
        onError(sc, msg);
        super.sendError(sc, msg);
      }

      /**
       * Use an empty stream to avoid container writing unwanted message when a resource is missing.
       * @param sc status code.
       * @param msg
       */
      private void onError(final int sc, final String msg) {
        LOG.debug("Error detected with code: {} and message: {}", sc, msg);
        final OutputStream emptyStream = new ByteArrayOutputStream();
        pw = new PrintWriter(emptyStream);
        sos = new DelegatingServletOutputStream(emptyStream);
      }

      @Override
      public ServletOutputStream getOutputStream()
          throws IOException {
        return sos;
      }

      /**
       * By default, redirect does not allow writing to output stream its content. In order to support this use-case, we
       * need to open a new connection and read the content manually.
       */
      @Override
      public void sendRedirect(final String location)
          throws IOException {
        try {
          LOG.debug("redirecting to: {}", location);
          final URL url = new URL(location);
          final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          // sets the "UseCaches" flag to <code>false</code>, mainly to avoid jar file locking on Windows.
          connection.setUseCaches(false);
          final InputStream is = connection.getInputStream();
          IOUtils.copy(is, sos);
          is.close();
        } catch (final IOException e) {
          LOG.warn("Invalid response for location: " + location);
          throw e;
        }
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