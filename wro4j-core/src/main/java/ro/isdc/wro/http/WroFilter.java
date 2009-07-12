/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.WroProcessResult;
import ro.isdc.wro.manager.WroSettings;
import ro.isdc.wro.manager.impl.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.util.Configuration;
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
  private static final Log log = LogFactory.getLog(WroFilter.class);

  /**
   * The name of the context parameter that specifies wroManager factory class
   */
  public static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";

  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode
   * is used.
   */
  public static final String PARAM_CONFIGURATION = "configuration";

  /**
   * Gzip resources param option.
   */
  public static final String PARAM_GZIP_RESOURCES = "gzipResources";

  /**
   * ServletContext.
   */
  private ServletContext servletContext;

  /**
   * Filter config.
   */
  private FilterConfig filterConfig;

  /**
   * Settings for configuration.
   */
  private WroSettings settings;

  /**
   * Flag for gziping resources. This value is true by default. Can be
   * configured in init-param.
   */
  private boolean gzipResources = true;

  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;

  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config) throws ServletException {
    this.servletContext = config.getServletContext();
    this.filterConfig = config;
    this.wroManagerFactory = getWroManagerFactory();
    initConfigParams();
    doInit(config);
  }

  /**
   * Custom filter initialization - can be used for extended classes.
   *
   * @see Filter#init(FilterConfig).
   */
  protected void doInit(final FilterConfig config) throws ServletException {}

  /**
   * Initialize {@link WroSettings} object.
   */
  private void initConfigParams() {
    // read gzip option
    final String gzipParam = this.filterConfig
        .getInitParameter(PARAM_GZIP_RESOURCES);
    gzipResources = gzipParam == null ? true : Boolean.valueOf(gzipParam);
    try {
      // read configuration
      final String configParam = this.filterConfig
          .getInitParameter(PARAM_CONFIGURATION);
      final Configuration config = configParam == null ? Configuration.DEVELOPMENT
          : Configuration.valueOf(configParam.toUpperCase());
      WroSettings.setConfiguration(config);
    } catch (final Exception e) {
      // the only exception can occurs is when calling Configuration.valueOf()
      // with invalid string
      throw new WroRuntimeException(
          "Invalid Configuration Init Parameter! Available values are: DEVELOPMENT or DEPLOYMENT");
    }
    log.info("Gziping is turned "
        + BooleanUtils.toStringOnOff(gzipResources).toUpperCase());
    log.info("WebResourceOptimizer is started in "
        + this.settings.getConfiguration() + " mode!");
  }

  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req,
      final ServletResponse res, final FilterChain chain) throws IOException,
      ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    // add request, response & servletContext to thread local
    ContextHolder.REQUEST_HOLDER.set(request);
    ContextHolder.RESPONSE_HOLDER.set(response);
    ContextHolder.SERVLET_CONTEXT_HOLDER.set(servletContext);

    final String requestURI = request.getRequestURI();
    final WroManager manager = wroManagerFactory.getInstance();
    // probably would be better to test with startsWith(servletContext +
    // resources)
    if (requestURI.contains(CssUrlRewritingProcessor.PATH_RESOURCES)) {
      final String resourceId = request
          .getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
      final InputStream is = manager.getUriLocatorFactory().getInstance(
          resourceId).locate(resourceId);
      if (is == null) {
        throw new WroRuntimeException("Could not Locate resource: "
            + resourceId);
      }
      final OutputStream os = response.getOutputStream();
      IOUtils.copy(is, os);
      is.close();
      os.close();
    } else {
      // process the uri using manager
      final WroProcessResult result = manager.process(requestURI);
      res.setContentType(result.getContentType());
      final InputStream is = result.getInputStream();
      OutputStream os = response.getOutputStream();
      if (shouldGzip()) {
        os = getGzipedOutputStream(response);
      }
      // append result to response stream
      IOUtils.copy(is, os);
      is.close();
      os.close();
    }
  }

  /**
   * Decision method for gziping resources.
   * @return true if requested resources should be gziped.
   */
  private boolean shouldGzip() {
    final HttpServletRequest request = ContextHolder.REQUEST_HOLDER.get();
    //final String toGzipAsString = request.getParameter("gzip");
    return acceptsEncoding(request, "gzip") && gzipResources;
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
      } catch (final ClassNotFoundException e) {
        throw new RuntimeException("", e);
      } catch (final InstantiationException e) {
        throw new RuntimeException("", e);
      } catch (final IllegalAccessException e) {
        throw new RuntimeException("", e);
      }
    }
  }

  /**
   * Checks if request accepts the named encoding.
   */
  private boolean acceptsEncoding(final HttpServletRequest request,
      final String name) {
    final boolean accepts = headerContains(request, "Accept-Encoding", name);
    return accepts;
  }

  /**
   * Checks if request contains the header value.
   */
  private boolean headerContains(final HttpServletRequest request,
      final String header, final String value) {
    final Enumeration<String> accepted = request.getHeaders(header);
    while (accepted.hasMoreElements()) {
      final String headerValue = accepted.nextElement();
      if (headerValue.indexOf(value) != -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {}

}
