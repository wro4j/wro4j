/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.WroProcessResult;
import ro.isdc.wro.manager.impl.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.util.WroUtil;

/**
 * WroFilter.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 31, 2008
 */
public class WroFilter implements Filter {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(WroFilter.class);
  private static final String CACHE_CONTROL_HEADER = "Cache-Control";
  private static final String CACHE_CONTROL_VALUE = "public, max-age=315360000, post-check=315360000, pre-check=315360000";
  private static final String LAST_MODIFIED_HEADER = "Last-Modified";
  private static final String LAST_MODIFIED_VALUE = "Sun, 06 Nov 2005 12:00:00 GMT";
  private static final String ETAG_HEADER = "ETag";
  private static final String ETAG_VALUE = "2740050219";
  private static final String EXPIRES_HEADER = "Expires";

  /**
   * The name of the context parameter that specifies wroManager factory class
   */
  public static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;

  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;

  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config) throws ServletException {
    this.filterConfig = config;
    this.wroManagerFactory = getWroManagerFactory();
    doInit(config);
  }

  /**
   * Custom filter initialization - can be used for extended classes.
   *
   * @see Filter#init(FilterConfig).
   */
  protected void doInit(final FilterConfig config) throws ServletException {}

  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req,
      final ServletResponse res, final FilterChain chain) throws IOException,
      ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    // add request, response & servletContext to thread local
    Context.set(new Context(request, response, filterConfig));
    final String requestURI = request.getRequestURI();
    final WroManager manager = wroManagerFactory.getInstance();

    if (null != request.getHeader("If-Modified-Since")) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      log.debug("Returning 'not modified' header. ");
      return;
    }

    setResponseHeaders(response);
    InputStream is = null;
    OutputStream os = null;
    if (requestURI.contains(CssUrlRewritingProcessor.PATH_RESOURCES)) {
      final String resourceId = request
          .getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
      is = manager.getUriLocatorFactory().getInstance(
          resourceId).locate(resourceId);
      if (is == null) {
        throw new WroRuntimeException("Could not Locate resource: "
            + resourceId);
      }
      os = response.getOutputStream();
    } else {
      // process the uri using manager
      final WroProcessResult result = manager.process(requestURI);
      response.setContentType(result.getContentType());
      is = result.getInputStream();
      os = response.getOutputStream();
      if (Context.get().isGzipEnabled()) {
        os = getGzipedOutputStream(response);
      }
      // append result to response stream
    }
    IOUtils.copy(is, os);
    is.close();
    os.close();
    //remove context from the current thread local.
    Context.unset();
  }

  /**
   * @param response
   */
  private void setResponseHeaders(final HttpServletResponse response) {
    // Force resource caching as best as possible
    response.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
    response.setHeader(LAST_MODIFIED_HEADER, LAST_MODIFIED_VALUE);
    response.setHeader(ETAG_HEADER, ETAG_VALUE);
    final Calendar cal = Calendar.getInstance();
    cal.roll(Calendar.YEAR, 10);
    response.setDateHeader(EXPIRES_HEADER, cal.getTimeInMillis());
  }

  /**
   * Add gzip header to response and wrap the response {@link OutputStream} with
   * {@link GZIPOutputStream}
   *
   * @param response
   *          {@link HttpServletResponse} object.
   * @return wrapped gziped OutputStream.
   */
  private OutputStream getGzipedOutputStream(final HttpServletResponse response)
      throws IOException {
    // gzip response
    WroUtil.addGzipHeader(response);
    // Create a gzip stream
    final OutputStream os = new GZIPOutputStream(response.getOutputStream());
    log.debug("Gziping outputStream response");
    return os;
  }

  /**
   * Factory method for {@link WroManagerFactory}. Override this method, in
   * order to change the way filter use factory.
   *
   * @return {@link WroManagerFactory} object.
   */
  protected WroManagerFactory getWroManagerFactory() {
    final String appFactoryClassName = filterConfig
        .getInitParameter(PARAM_MANAGER_FACTORY);
    if (appFactoryClassName == null) {
      // If no context param was specified we return the default factory
      return new ServletContextAwareWroManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader()
            .loadClass(appFactoryClassName);
        // Instantiate the factory
        return (WroManagerFactory) factoryClass.newInstance();
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception while loading WroManagerFactory class", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }

}
