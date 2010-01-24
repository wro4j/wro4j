/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.impl.ServletContextAwareWroManagerFactory;


/**
 * Main entry point. Perform the request processing by identifying the type of the requested resource. Depending on the
 * way it is configured, it builds
 *
 * @author Alex Objelean
 * @created Created on Oct 31, 2008
 */
public class WroFilter
  implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(WroFilter.class);
  /**
   * The name of the context parameter that specifies wroManager factory class
   */
  private static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;

  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;
  /**
   * Cache control header values
   */
  private String etagValue;
  private long lastModifiedValue;
  private String cacheControlValue;
  private long expiresValue;


  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
    throws ServletException {
    this.filterConfig = config;
    this.wroManagerFactory = getWroManagerFactory();
    initHeaderValues();
    doInit(config);
  }


  /**
   * Initialize header values used for server-side resource caching.
   */
  private void initHeaderValues() {
    etagValue = UUID.randomUUID().toString();
    lastModifiedValue = new Date().getTime();
    cacheControlValue = "public, max-age=315360000, post-check=315360000, pre-check=315360000";
    final Calendar cal = Calendar.getInstance();
    cal.roll(Calendar.YEAR, 10);
    expiresValue = cal.getTimeInMillis();
  }


  /**
   * Custom filter initialization - can be used for extended classes.
   *
   * @see Filter#init(FilterConfig).
   */
  protected void doInit(final FilterConfig config)
    throws ServletException {}


  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
    throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest)req;
    final HttpServletResponse response = (HttpServletResponse)res;

    // add request, response & servletContext to thread local
    Context.set(new Context(request, response, filterConfig));
    final WroManager manager = wroManagerFactory.getInstance();

    if (!Context.get().isDevelopmentMode()) {
      final String ifNoneMatch = request.getHeader(HttpHeader.IF_NONE_MATCH.toString());
      if (etagValue.equals(ifNoneMatch)) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }
    }
    setResponseHeaders(response);
    // process the uri using manager
    manager.process(request, response);
    // remove context from the current thread local.
    Context.unset();
  }


  /**
   * Method responsible for setting response headers, used mostly for cache control. Override this method if you want to
   * change the way headers are set.<br>
   * Default implementation will set
   *
   * @param response {@link HttpServletResponse} object.
   */
  protected void setResponseHeaders(final HttpServletResponse response) {
    if (!Context.get().isDevelopmentMode()) {
      // Force resource caching as best as possible
      response.setHeader(HttpHeader.CACHE_CONTROL.toString(), cacheControlValue);
      response.setHeader(HttpHeader.ETAG.toString(), etagValue);
      response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), lastModifiedValue);
      response.setDateHeader(HttpHeader.EXPIRES.toString(), expiresValue);
    }
  }

  /**
   * Factory method for {@link WroManagerFactory}. Override this method, in order to change the way filter use factory.
   *
   * @return {@link WroManagerFactory} object.
   */
  protected WroManagerFactory getWroManagerFactory() {
    final String appFactoryClassName = filterConfig.getInitParameter(PARAM_MANAGER_FACTORY);
    if (appFactoryClassName == null) {
      // If no context param was specified we return the default factory
      return new ServletContextAwareWroManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader().loadClass(appFactoryClassName);
        // Instantiate the factory
        return (WroManagerFactory)factoryClass.newInstance();
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception while loading WroManagerFactory class", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void destroy() {
    wroManagerFactory.destroy();
  }

}
